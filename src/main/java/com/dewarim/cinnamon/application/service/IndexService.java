package com.dewarim.cinnamon.application.service;

import com.dewarim.cinnamon.api.content.ContentProvider;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.application.exception.CinnamonException;
import com.dewarim.cinnamon.application.service.index.ContentContainer;
import com.dewarim.cinnamon.configuration.LuceneConfig;
import com.dewarim.cinnamon.dao.FolderDao;
import com.dewarim.cinnamon.dao.FolderMetaDao;
import com.dewarim.cinnamon.dao.IndexItemDao;
import com.dewarim.cinnamon.dao.IndexJobDao;
import com.dewarim.cinnamon.dao.OsdDao;
import com.dewarim.cinnamon.dao.OsdMetaDao;
import com.dewarim.cinnamon.dao.RelationDao;
import com.dewarim.cinnamon.model.Folder;
import com.dewarim.cinnamon.model.IndexItem;
import com.dewarim.cinnamon.model.Meta;
import com.dewarim.cinnamon.model.ObjectSystemData;
import com.dewarim.cinnamon.model.index.IndexJob;
import com.dewarim.cinnamon.model.index.IndexJobType;
import com.dewarim.cinnamon.model.relations.Relation;
import com.dewarim.cinnamon.provider.ContentProviderService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.SingleInstanceLockFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.dewarim.cinnamon.api.Constants.LUCENE_FIELD_CINNAMON_CLASS;
import static com.dewarim.cinnamon.api.Constants.LUCENE_FIELD_UNIQUE_ID;

public class IndexService implements Runnable {
    private static final Logger log = LogManager.getLogger(IndexService.class);

    public static final byte[]  NO_CONTENT    = new byte[0];
    public static       boolean isInitialized = false;

    private       boolean      stopped   = false;
    private       IndexWriter  indexWriter;
    private final OsdDao       osdDao    = new OsdDao();
    private final FolderDao    folderDao = new FolderDao();
    private final LuceneConfig config;
    private final Path         indexPath;
    private final XmlMapper    xmlMapper = new XmlMapper();

    private final List<IndexItem> indexItems;

    public IndexService(LuceneConfig config) {
        this.config = config;
        indexPath = Paths.get(config.getIndexPath());
        if (!indexPath.toFile().exists()) {
            boolean madeDirs = indexPath.toFile().mkdirs();
            if (madeDirs) {
                throw new IllegalStateException("Could not create path to index: " + indexPath.toAbsolutePath());
            }
        }
        indexItems = new IndexItemDao().list();
    }


    public void run() {
        log.info("IndexService thread is running");
        int limit = 100;
        try (Analyzer standardAnalyzer = new StandardAnalyzer();
             Directory indexDir = FSDirectory.open(indexPath, new SingleInstanceLockFactory())) {

            while (!stopped) {
                try (SqlSession sqlSession = ThreadLocalSqlSession.getNewSession(TransactionIsolationLevel.READ_COMMITTED)) {
                    IndexWriterConfig writerConfig = new IndexWriterConfig(standardAnalyzer);
                    writerConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
                    writerConfig.setCommitOnClose(true);
                    indexWriter = new IndexWriter(indexDir, writerConfig);

                    Set<IndexKey> seen   = new HashSet<>(128);
                    IndexJobDao   jobDao = new IndexJobDao().setSqlSession(sqlSession);
                    osdDao.setSqlSession(sqlSession);
                    folderDao.setSqlSession(sqlSession);

                    while (jobDao.countJobs() > 0) {
                        List<IndexJob> jobs = jobDao.getIndexJobsByFailedCountWithLimit(0, limit);
                        log.debug("Found " + jobs.size() + " IndexJobs.");
                        if (jobs.size() == 0) {
                            break;
                        }
                        boolean        indexChanged = false;
                        List<IndexJob> jobsToDelete = new ArrayList<>();
                        for (IndexJob job : jobs) {
                            IndexKey indexKey = new IndexKey(job.getJobType(), job.getItemId());
                            if (seen.contains(indexKey)) {
                                // remove duplicate jobs in the current transaction
                                jobDao.delete(job);
                                continue;
                            }

                            try {
                                switch (job.getAction()) {
                                    case DELETE -> deleteFromIndex(indexKey);
                                    case CREATE, UPDATE -> handleIndexItem(job, indexKey, indexItems);
                                }
                                jobsToDelete.add(job);
                                indexChanged = true;
                            } catch (Exception e) {
                                log.info("IndexJob failed with: ", e);
                                job.setFailed(job.getFailed() + 1);
                                jobDao.updateStatus(job);
                            }
                            seen.add(indexKey);
                        }
                        if (indexChanged) {
                            long sequenceNr = indexWriter.commit();
                            log.debug("sequenceNr: " + sequenceNr);
                        } else {
                            log.debug("no change to index");
                        }
                        jobsToDelete.forEach(jobDao::delete);
                        jobDao.commit();
                    }
                    indexWriter.close();
                    // TODO: avoid busy waiting
                    Thread.sleep(config.getMillisToWaitBetweenRuns());
                    seen.clear();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("Index thread was interrupted.");
                } catch (Exception e) {
                    log.warn("Failed to index: ", e);
                } finally {
                    if (indexWriter.isOpen()) {
                        indexWriter.close();
                    }
                    // signals SearchService it's okay to open the (potentially new) index for reading
                    isInitialized = true;
                }
            }
        } catch (Exception e) {
            log.error("Lucene Index Loop failed: ", e);
        }


    }

    private void handleIndexItem(IndexJob job, IndexKey key, List<IndexItem> indexItems) throws IOException {
        Document doc = new Document();
        doc.add(new StringField(LUCENE_FIELD_UNIQUE_ID, key.toString(), Field.Store.NO));
        doc.add(new Field(LUCENE_FIELD_CINNAMON_CLASS, key.type.toString(), StringField.TYPE_STORED));

        boolean isOkay = false;
        switch (job.getJobType()) {
            case OSD -> isOkay = indexOsd(job.getItemId(), doc, indexItems);
            case FOLDER -> isOkay = indexFolder(job.getItemId(), doc, indexItems);
        }
        if (!isOkay) {
            log.info("Failed to index " + job);
            return;
        }
        switch (job.getAction()) {
            case UPDATE -> indexWriter.updateDocument(new Term(LUCENE_FIELD_UNIQUE_ID, key.toString()), doc);
            case CREATE -> indexWriter.addDocument(doc);
        }
    }

    private boolean indexFolder(Long id, Document doc, List<IndexItem> indexItems) throws JsonProcessingException {
        Optional<Folder> folderOpt = folderDao.getFolderById(id);
        if (folderOpt.isEmpty()) {
            log.debug("Folder " + id + " not found for indexing.");
            return false;
        }
        Folder folder = folderOpt.get();
        //// index sysMeta

        // folderpath
        String folderPath = folderDao.getFolderPath(folder.getParentId());
        doc.add(new StringField("folderpath", folderPath, Field.Store.NO));

        doc.add(new StoredField("acl", folder.getAclId()));
        doc.add(new NumericDocValuesField("acl", folder.getAclId()));
        // option: add a NumericDocValueField for scoring by age
        doc.add(new NumericDocValuesField("id", folder.getId()));
        doc.add(new StoredField("id", folder.getId()));
        doc.add(new StringField("created", DateTools.dateToString(folder.getCreated(), DateTools.Resolution.MILLISECOND), Field.Store.NO));
        doc.add(new LongPoint("created", folder.getCreated().getTime()));
        doc.add(new StringField("name", folder.getName(), Field.Store.NO));
        doc.add(new NumericDocValuesField("owner", folder.getOwnerId()));
        doc.add(new StoredField("owner", folder.getOwnerId()));
        if (folder.getParentId() != null) {
            doc.add(new NumericDocValuesField("parent", folder.getParentId()));
        }
        doc.add(new TextField("summary", folder.getSummary(), Field.Store.NO));
        doc.add(new NumericDocValuesField("type", folder.getTypeId()));

        List<Meta> metas = new FolderMetaDao().listByFolderId(folder.getId());
        folder.setMetas(metas);
        applyIndexItems(doc, indexItems, xmlMapper.writeValueAsString(folder), NO_CONTENT);
        return true;
    }

    private boolean indexOsd(Long id, Document doc, List<IndexItem> indexItems) throws JsonProcessingException {
        Optional<ObjectSystemData> osdOpt = osdDao.getObjectById(id);
        if (osdOpt.isEmpty()) {
            log.debug("osd " + id + " not found for indexing");
            return false;
        }
        ObjectSystemData osd = osdOpt.get();

        // index sysMeta
        String folderPath = folderDao.getFolderPath(osd.getParentId());
        doc.add(new StringField("folderpath", folderPath, Field.Store.NO));

        doc.add(new StoredField("acl", osd.getAclId()));
        doc.add(new NumericDocValuesField("acl", osd.getAclId()));
        doc.add(new StringField("cmn_version", osd.getCmnVersion(), Field.Store.NO));
        doc.add(new StringField("content_changed", String.valueOf(osd.isContentChanged()), Field.Store.NO));
        if (osd.getContentSize() != null) {
            doc.add(new NumericDocValuesField("content_size", osd.getContentSize()));
        }
        // option: add a NumericDocValueField for scoring by age
        doc.add(new StringField("created", DateTools.dateToString(osd.getCreated(), DateTools.Resolution.MILLISECOND), Field.Store.NO));
        doc.add(new LongPoint("created", osd.getCreated().getTime()));
        doc.add(new StringField("modified", DateTools.dateToString(osd.getCreated(), DateTools.Resolution.MILLISECOND), Field.Store.NO));
        doc.add(new LongPoint("modified", osd.getCreated().getTime()));

        doc.add(new NumericDocValuesField("creator", osd.getCreatorId()));
        doc.add(new NumericDocValuesField("modifier", osd.getModifierId()));
        if (osd.getFormatId() != null) {
            doc.add(new NumericDocValuesField("format", osd.getFormatId()));
        }
        doc.add(new NumericDocValuesField("id", osd.getId()));
        doc.add(new StoredField("id", osd.getId()));
        if (osd.getLanguageId() != null) {
            doc.add(new NumericDocValuesField("language", osd.getLanguageId()));
        }
        doc.add(new StringField("latest_branch", String.valueOf(osd.isLatestBranch()), Field.Store.NO));
        doc.add(new StringField("latest_head", String.valueOf(osd.isLatestHead()), Field.Store.NO));
        if (osd.getLockerId() != null) {
            doc.add(new NumericDocValuesField("locker", osd.getId()));
        }
        doc.add(new StringField("metadata_changed", String.valueOf(osd.isMetadataChanged()), Field.Store.NO));
        doc.add(new StringField("name", osd.getName(), Field.Store.NO));
        doc.add(new NumericDocValuesField("owner", osd.getOwnerId()));
        doc.add(new StoredField("owner", osd.getOwnerId()));
        doc.add(new NumericDocValuesField("parent", osd.getParentId()));
        if (osd.getPredecessorId() != null) {
            doc.add(new NumericDocValuesField("predecessor", osd.getPredecessorId()));
        }
        if (osd.getRootId() != null) {
            doc.add(new NumericDocValuesField("root", osd.getRootId()));
        }
        if (osd.getLifecycleStateId() != null) {
            doc.add(new NumericDocValuesField("lifecycle_state", osd.getLifecycleStateId()));
        }
        doc.add(new TextField("summary", osd.getSummary(), Field.Store.NO));
        doc.add(new NumericDocValuesField("type", osd.getTypeId()));

        List<Long>     relationCriteria = List.of(osd.getId());
        List<Relation> relations        = new RelationDao().getRelationsOrMode(relationCriteria, relationCriteria, Collections.emptyList(), true);
        osd.setRelations(relations);

        List<Meta> metas = new OsdMetaDao().listByOsd(osd.getId());
        osd.setMetas(metas);

        byte[] content = NO_CONTENT;
        if (osd.getContentPath() != null) {
            ContentProvider contentProvider = ContentProviderService.getInstance().getContentProvider(osd.getContentProvider());
            // TODO: depending on contenttype, load content or use NO_CONTENT
            // for example, do not try to parse JPEG to XML (we will use Apache Tika to create a metaset for the metadata)
            // LuceneConfig should have list of contenttypes which may be indexed - as long as a flag "pure text" for
            // content that needs to be wrapped as <content>$PURE_TEXT_CONTENT</content> for proper parsing.
            try (InputStream contentStream = contentProvider.getContentStream(osd)) {
                // performance: maybe detect <xml>-Content here before reading all the bytes of a DVD.iso etc.
                content = contentStream.readAllBytes();
            } catch (IOException e) {
                throw new CinnamonException("Failed to load content for OSD " + osd.getId() + " at " + osd.getContentPath(), e);
            }
        }
        applyIndexItems(doc, indexItems, xmlMapper.writeValueAsString(osd), content);
        return true;

    }

    private void applyIndexItems(Document luceneDoc, List<IndexItem> indexItems,
                                 String objectAsString, byte[] content) {
        ContentContainer   contentContainer = new ContentContainer(objectAsString, content);
        org.dom4j.Document xmlDoc           = contentContainer.getCombinedDocument();

        for (IndexItem indexItem : indexItems) {
            String fieldName    = indexItem.getFieldName();
            String searchString = indexItem.getSearchString();
            // TODO: check search condition, probably with xmlDoc
            if (xmlDoc.valueOf(indexItem.getSearchCondition()).equals("true")) {
                indexItem.getIndexType().getIndexer()
                        .indexObject(xmlDoc, contentContainer.asNode(), luceneDoc, fieldName, searchString, indexItem.isMultipleResults());
            } else {
                log.debug("searchCondition failed: " + indexItem.getSearchCondition());
            }
        }
    }

    private void deleteFromIndex(IndexKey key) throws IOException {
        indexWriter.deleteDocuments(new Term(LUCENE_FIELD_UNIQUE_ID, key.toString()));
    }

    public void setStopped(boolean stopped) {
        this.stopped = stopped;
    }

    static class IndexKey {
        private final IndexJobType type;
        private final Long         itemId;

        public IndexKey(IndexJobType type, Long itemId) {
            this.type = type;
            this.itemId = itemId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            IndexKey indexKey = (IndexKey) o;
            return type == indexKey.type && itemId.equals(indexKey.itemId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, itemId);
        }

        @Override
        public String toString() {
            return type.name() + "#" + itemId;
        }
    }


    enum IndexMode {
        CREATE, UPDATE
    }

    public void addIndexItems(List<IndexItem> indexItems) {
        this.indexItems.addAll(indexItems);
    }
    public void removeIndexItems(List<Long> ids){
        this.indexItems.removeAll(this.indexItems.stream().filter(item -> ids.contains(item.getId())).collect(Collectors.toSet()));
    }
    public void updateIndexItems(List<IndexItem> indexItems){
        Map<Long,IndexItem> newItems = indexItems.stream().collect(Collectors.toMap(IndexItem::getId, Function.identity()));
        Map<Long,IndexItem> oldItems = this.indexItems.stream().collect(Collectors.toMap(IndexItem::getId, Function.identity()));
        newItems.forEach( (id,value) -> {
            if(oldItems.containsKey(id)){
                this.indexItems.remove(oldItems.get(id));
                this.indexItems.add(value);
            }
        });
    }
}
