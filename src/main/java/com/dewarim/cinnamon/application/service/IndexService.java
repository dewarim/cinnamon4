package com.dewarim.cinnamon.application.service;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.content.ContentProvider;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.application.exception.CinnamonException;
import com.dewarim.cinnamon.application.service.index.ContentContainer;
import com.dewarim.cinnamon.configuration.LuceneConfig;
import com.dewarim.cinnamon.dao.*;
import com.dewarim.cinnamon.model.*;
import com.dewarim.cinnamon.model.index.IndexJob;
import com.dewarim.cinnamon.model.index.IndexJobType;
import com.dewarim.cinnamon.model.relations.Relation;
import com.dewarim.cinnamon.provider.ContentProviderService;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.SingleInstanceLockFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.dewarim.cinnamon.api.Constants.LUCENE_FIELD_CINNAMON_CLASS;
import static com.dewarim.cinnamon.api.Constants.LUCENE_FIELD_UNIQUE_ID;

public class IndexService implements Runnable {
    private static final Logger log = LogManager.getLogger(IndexService.class);

    public static final byte[] NO_CONTENT = new byte[0];

    public static boolean isInitialized = false;

    private       boolean         stopped   = false;
    private       IndexWriter     indexWriter;
    private final OsdDao          osdDao    = new OsdDao();
    private final FolderDao       folderDao = new FolderDao();
    private final LuceneConfig    config;
    private final Path            indexPath;
    private final XmlMapper       xmlMapper;
    private final TikaService     tikaService;
    private final List<IndexItem> indexItems;

    public IndexService(LuceneConfig config, TikaService tikaService) {
        this.config = config;
        indexPath   = Paths.get(config.getIndexPath());
        if (!indexPath.toFile().exists()) {
            boolean madeDirs = indexPath.toFile().mkdirs();
            if (madeDirs) {
                throw new IllegalStateException("Could not create path to index: " + indexPath.toAbsolutePath());
            }
        }
        indexItems       = new IndexItemDao().list();
        xmlMapper        = new XmlMapper();
        this.tikaService = tikaService;
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
                        List<IndexJob> jobs = jobDao.getIndexJobsByFailedCountWithLimit(config.getMaxIndexAttempts(), limit);
                        if (jobs.isEmpty()) {
                            log.trace("Found 0 IndexJobs.");
                            break;
                        }
                        log.debug("Found {} IndexJobs with not more than {} failed attempts.", jobs.size(), config.getMaxIndexAttempts());
                        List<IndexJob> jobsToDelete  = new ArrayList<>();
                        List<IndexJob> jobsToUpdate  = new ArrayList<>();
                        AtomicInteger  changeCounter = new AtomicInteger();
                        AtomicBoolean  error         = new AtomicBoolean(false);

                        // TODO: use parallelStream ... currently, this will exhaust all DB connections since each Thread has a ThreadLocal session.
                        jobs.forEach(job -> {
                            IndexKey indexKey = new IndexKey(job.getJobType(), job.getItemId());
                            if (seen.contains(indexKey)) {
                                // remove duplicate jobs in the current transaction
                                jobsToDelete.add(job);
                                return;
                            }

                            IndexResult indexResult;
                            switch (job.getAction()) {
                                case DELETE -> indexResult = deleteFromIndex(indexKey);
                                case CREATE, UPDATE -> {
                                    try {
                                        indexResult = handleIndexItem(job, indexKey, indexItems);
                                    } catch (IOException e) {
                                        log.error("Failed to handle IndexItem: {}", indexKey, e);
                                        indexResult = IndexResult.ERROR;
                                    }
                                }
                                default -> indexResult = IndexResult.IGNORE;
                            }

                            switch (indexResult) {
                                case ERROR -> {
                                    error.set(true);
                                    // TODO add to list of jobs to update
                                    job.setFailed(job.getFailed() + 1);
                                }
                                case SUCCESS -> {
                                    jobsToDelete.add(job);
                                    changeCounter.incrementAndGet();
                                }
                                case FAILED -> {
                                    job.setFailed(job.getFailed() + 1);
                                    // TODO add to list of jobs to update
                                    jobsToUpdate.add(job);
                                }
                                case IGNORE, NO_OBJECT -> {
                                }
                            }
                            seen.add(indexKey);
                        });

                        if (error.get()) {
                            log.error("Error during indexing; trying to roll back indexWriter.");
                            indexWriter.rollback();
                            log.error("Also trying to mark jobs as failed after index error");
                            List<IndexJob> allJobs = new ArrayList<>(jobsToUpdate);
                            allJobs.addAll(jobsToDelete);
                            allJobs.forEach(job -> {
                                job.setFailed(job.getFailed() + 1);
                                jobDao.updateStatus(job);
                            });
                            jobDao.commit();
                            log.error("... trying to continue with indexing.");
                            // TODO: add status message
                            continue;
                        }

                        if (changeCounter.get() > 0) {
                            long sequenceNr = indexWriter.commit();
                            log.debug("sequenceNr: {}", sequenceNr);
                        }
                        else {
                            log.debug("no change to index");
                        }

                        if (!jobsToDelete.isEmpty()) {
                            log.debug("deleting {} index jobs", jobsToDelete.size());
                            jobsToDelete.forEach(jobDao::delete);
                        }
                        if (!jobsToUpdate.isEmpty()) {
                            log.debug("updating {} index jobs", jobsToUpdate.size());
                            jobsToUpdate.forEach(jobDao::updateStatus);
                        }
                        jobDao.commit();
                    }
                    indexWriter.close();
                    // TODO: avoid busy waiting -> use blocking queue or semaphore?
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

    private IndexResult handleIndexItem(IndexJob job, IndexKey key, List<IndexItem> indexItems) throws IOException {
        Document doc = new Document();
        doc.add(new StringField(LUCENE_FIELD_UNIQUE_ID, key.toString(), Field.Store.NO));
        doc.add(new Field(LUCENE_FIELD_CINNAMON_CLASS, key.type.toString(), StringField.TYPE_STORED));

        IndexResult indexResult;
        switch (job.getJobType()) {
            case OSD -> indexResult = indexOsd(job.getItemId(), doc, indexItems, job.isUpdateTikaMetaset());
            case FOLDER -> indexResult = indexFolder(job.getItemId(), doc, indexItems);
            default -> indexResult = IndexResult.IGNORE;
        }

        switch (indexResult) {
            case FAILED -> log.info("Failed to index " + job);
            case SUCCESS -> {
                switch (job.getAction()) {
                    case UPDATE -> indexWriter.updateDocument(new Term(LUCENE_FIELD_UNIQUE_ID, key.toString()), doc);
                    case CREATE -> indexWriter.addDocument(doc);
                }
            }
            default -> {
            }
        }
        return indexResult;
    }

    enum IndexResult {
        SUCCESS,
        NO_OBJECT,
        FAILED,
        ERROR, IGNORE
    }

    private IndexResult indexFolder(Long id, Document doc, List<IndexItem> indexItems) {
        Optional<Folder> folderOpt = folderDao.getFolderById(id);
        if (folderOpt.isEmpty()) {
            log.debug("Folder " + id + " not found for indexing.");
            return IndexResult.NO_OBJECT;
        }
        Folder folder = folderOpt.get();
        try {
            //// index sysMeta
            // folderpath
            String folderPath = folderDao.getFolderPath(folder.getParentId());
            log.debug("folderpath: " + folderPath);
            doc.add(new StringField("folderpath", folderPath.toLowerCase(), Field.Store.NO));

            doc.add(new StoredField("acl", folder.getAclId()));
            doc.add(new LongPoint("acl", folder.getAclId()));
            // option: add a NumericDocValueField for scoring by age
            doc.add(new LongPoint("id", folder.getId()));
            doc.add(new StoredField("id", folder.getId()));
            doc.add(new StringField("created", DateTools.dateToString(folder.getCreated(), DateTools.Resolution.MILLISECOND), Field.Store.NO));
            doc.add(new LongPoint("created", folder.getCreated().getTime()));
            doc.add(new StringField("name", folder.getName().toLowerCase(), Field.Store.NO));
            doc.add(new LongPoint("owner", folder.getOwnerId()));
            doc.add(new StoredField("owner", folder.getOwnerId()));
            if (folder.getParentId() != null) {
                doc.add(new LongPoint("parent", folder.getParentId()));
            }
            doc.add(new TextField("summary", folder.getSummary(), Field.Store.NO));
            doc.add(new LongPoint("folder_type", folder.getTypeId()));

            FolderMetaDao metaDao = new FolderMetaDao(TransactionIsolationLevel.READ_COMMITTED);
            List<Meta>    metas   = metaDao.listByFolderId(folder.getId());
            metaDao.closePrivateSession();
            folder.setMetas(metas);

            applyIndexItems(doc, indexItems, xmlMapper.writeValueAsString(folder), NO_CONTENT, folderPath);
        } catch (Exception e) {
            log.warn("Failed to index folder #" + id, e);
            return IndexResult.FAILED;
        }
        return IndexResult.SUCCESS;
    }

    private IndexResult indexOsd(Long id, Document doc, List<IndexItem> indexItems, boolean updateTikaMetaset) {
        Optional<ObjectSystemData> osdOpt = osdDao.getObjectById(id);
        if (osdOpt.isEmpty()) {
            log.debug("osd " + id + " not found for indexing");
            return IndexResult.NO_OBJECT;
        }
        ObjectSystemData osd = osdOpt.get();
        try {
            // index sysMeta
            String folderPath = folderDao.getFolderPath(osd.getParentId());
            doc.add(new StringField("folderpath", folderPath.toLowerCase(), Field.Store.NO));

            doc.add(new StoredField("acl", osd.getAclId()));
            doc.add(new LongPoint("acl", osd.getAclId()));
            doc.add(new StringField("cmn_version", osd.getCmnVersion(), Field.Store.NO));
            doc.add(new StringField("content_changed", String.valueOf(osd.isContentChanged()), Field.Store.NO));
            if (osd.getContentSize() != null) {
                doc.add(new LongPoint("content_size", osd.getContentSize()));
            }
            // option: add a NumericDocValueField for scoring by age
            doc.add(new StringField("created", DateTools.dateToString(osd.getCreated(), DateTools.Resolution.MILLISECOND), Field.Store.NO));
            doc.add(new LongPoint("created", osd.getCreated().getTime()));
            doc.add(new StringField("modified", DateTools.dateToString(osd.getCreated(), DateTools.Resolution.MILLISECOND), Field.Store.NO));
            doc.add(new LongPoint("modified", osd.getCreated().getTime()));

            doc.add(new LongPoint("creator", osd.getCreatorId()));
            doc.add(new LongPoint("modifier", osd.getModifierId()));
            if (osd.getFormatId() != null) {
                doc.add(new LongPoint("format", osd.getFormatId()));
            }
            doc.add(new LongPoint("id", osd.getId()));
            doc.add(new StoredField("id", osd.getId()));
            if (osd.getLanguageId() != null) {
                doc.add(new LongPoint("language", osd.getLanguageId()));
            }
            doc.add(new StringField("latest_branch", String.valueOf(osd.isLatestBranch()), Field.Store.NO));
            doc.add(new StringField("latest_head", String.valueOf(osd.isLatestHead()), Field.Store.NO));
            if (osd.getLockerId() != null) {
                log.debug("locker: {}", osd.getLockerId());
                doc.add(new LongPoint("locker", osd.getLockerId()));
            }
            doc.add(new StringField("metadata_changed", String.valueOf(osd.isMetadataChanged()), Field.Store.NO));
            doc.add(new StringField("name", osd.getName().toLowerCase(), Field.Store.NO));
            doc.add(new LongPoint("owner", osd.getOwnerId()));
            doc.add(new StoredField("owner", osd.getOwnerId()));
            doc.add(new LongPoint("parent", osd.getParentId()));
            if (osd.getPredecessorId() != null) {
                doc.add(new LongPoint("predecessor", osd.getPredecessorId()));
            }
            if (osd.getRootId() != null) {
                doc.add(new LongPoint("root", osd.getRootId()));
            }
            if (osd.getLifecycleStateId() != null) {
                doc.add(new LongPoint("lifecycle_state", osd.getLifecycleStateId()));
            }
            doc.add(new TextField("summary", osd.getSummary(), Field.Store.NO));
            doc.add(new LongPoint("object_type", osd.getTypeId()));

            List<Long>     relationCriteria = List.of(osd.getId());
            List<Relation> relations        = new RelationDao().getRelationsOrMode(relationCriteria, relationCriteria, Collections.emptyList(), true);
            for (Relation relation : relations) {
                relation.setParent(relation.getLeftId().equals(osd.getId()));
            }
            osd.setRelations(relations);

            byte[] content = NO_CONTENT;
            if (osd.getContentPath() != null && osd.getFormatId() != null) {
                Format          format          = new FormatDao().getObjectById(osd.getFormatId()).orElseThrow(() -> new CinnamonException(ErrorCode.FORMAT_NOT_FOUND.getCode()));
                ContentProvider contentProvider = ContentProviderService.getInstance().getContentProvider(osd.getContentProvider());
                try (InputStream contentStream = contentProvider.getContentStream(osd)) {
                    switch (format.getIndexMode()) {
                        case XML -> content = contentStream.readAllBytes();
                        case TIKA -> {
                            // TODO: add test for this
                            if (updateTikaMetaset && tikaService.isEnabled()) {
                                log.debug("update tika metaset");
                                tikaService.convertContentToTikaMetaset(osd, contentStream, format);
                            }
                            else {
                                log.debug("ignore format with tika flag");
                            }
                        }
                        case PLAIN_TEXT -> content = ("<plainText>" + new String(contentStream.readAllBytes(), StandardCharsets.UTF_8) + "</plainText>").getBytes();
                    }
                } catch (IOException e) {
                    throw new CinnamonException("Failed to load content for OSD " + osd.getId() + " at " + osd.getContentPath(), e);
                }
            }
            OsdMetaDao metaDao = new OsdMetaDao(TransactionIsolationLevel.READ_COMMITTED);
            List<Meta> metas   = metaDao.listByOsd(osd.getId());
            metaDao.closePrivateSession();
            osd.setMetas(metas);

            applyIndexItems(doc, indexItems, xmlMapper.writeValueAsString(osd), content, folderPath);
        } catch (Exception e) {
            log.warn("Indexing failed for OSD #" + id, e);
            return IndexResult.FAILED;
        }
        return IndexResult.SUCCESS;

    }

    private void applyIndexItems(Document luceneDoc, List<IndexItem> indexItems,
                                 String objectAsString, byte[] content, String folderPath) {
        ContentContainer   contentContainer = new ContentContainer(objectAsString, content, folderPath);
        org.dom4j.Document xmlDoc           = contentContainer.getCombinedDocument();

        for (IndexItem indexItem : indexItems) {
            String fieldName    = indexItem.getFieldName();
            String searchString = indexItem.getSearchString();
            log.debug("indexing for field: " + fieldName + " with " + searchString);
            // TODO: check search condition, probably with xmlDoc
            if (xmlDoc.valueOf(indexItem.getSearchCondition()).equals("true")) {
                indexItem.getIndexType().getIndexer()
                        .indexObject(xmlDoc, contentContainer.asNode(), luceneDoc, fieldName, searchString, indexItem.isMultipleResults());
            }
            else {
                log.debug("searchCondition failed: " + indexItem.getSearchCondition());
            }
        }
    }

    private IndexResult deleteFromIndex(IndexKey key) {
        try {
            indexWriter.deleteDocuments(new Term(LUCENE_FIELD_UNIQUE_ID, key.toString()));
            return IndexResult.SUCCESS;
        } catch (Exception e) {
            log.warn("Failed to delete document from Lucene index: {}", key);
            return IndexResult.FAILED;
        }
    }

    public void setStopped(boolean stopped) {
        this.stopped = stopped;
    }

    static class IndexKey {
        private final IndexJobType type;
        private final Long         itemId;

        public IndexKey(IndexJobType type, Long itemId) {
            this.type   = type;
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

    public void addIndexItems(List<IndexItem> indexItems) {
        this.indexItems.addAll(indexItems);
    }

    public void removeIndexItems(List<Long> ids) {
        this.indexItems.removeAll(this.indexItems.stream().filter(item -> ids.contains(item.getId())).collect(Collectors.toSet()));
    }

    public void updateIndexItems(List<IndexItem> indexItems) {
        Map<Long, IndexItem> newItems = indexItems.stream().collect(Collectors.toMap(IndexItem::getId, Function.identity()));
        Map<Long, IndexItem> oldItems = this.indexItems.stream().collect(Collectors.toMap(IndexItem::getId, Function.identity()));
        newItems.forEach((id, value) -> {
            if (oldItems.containsKey(id)) {
                this.indexItems.remove(oldItems.get(id));
                this.indexItems.add(value);
            }
        });
    }
}
