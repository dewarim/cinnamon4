package com.dewarim.cinnamon.application.service;

import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.configuration.LuceneConfig;
import com.dewarim.cinnamon.dao.FolderDao;
import com.dewarim.cinnamon.dao.IndexItemDao;
import com.dewarim.cinnamon.dao.IndexJobDao;
import com.dewarim.cinnamon.dao.OsdDao;
import com.dewarim.cinnamon.model.Folder;
import com.dewarim.cinnamon.model.IndexItem;
import com.dewarim.cinnamon.model.ObjectSystemData;
import com.dewarim.cinnamon.model.index.IndexJob;
import com.dewarim.cinnamon.model.index.IndexJobType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.SingleInstanceLockFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static com.dewarim.cinnamon.api.Constants.LUCENE_FIELD_CINNAMON_CLASS;
import static com.dewarim.cinnamon.api.Constants.LUCENE_FIELD_UNIQUE_ID;

public class IndexService implements Runnable {
    private static final Logger log = LogManager.getLogger(IndexService.class);

    public static boolean isInitialized = false;

    private       boolean      stopped   = false;
    private       IndexWriter  indexWriter;
    private final OsdDao       osdDao    = new OsdDao();
    private final FolderDao    folderDao = new FolderDao();
    private final LuceneConfig config;
    private final Path         indexPath;

    public IndexService(LuceneConfig config) {
        this.config = config;
        indexPath = Paths.get(config.getIndexPath());
        if (!indexPath.toFile().exists()) {
            boolean madeDirs = indexPath.toFile().mkdirs();
            if (madeDirs) {
                throw new IllegalStateException("Could not create path to index: " + indexPath.toAbsolutePath());
            }
        }
    }


    public void run() {
        log.info("IndexService thread is running");
        int limit = 100;
        try (Analyzer standardAnalyzer = new StandardAnalyzer();
             Directory indexDir = FSDirectory.open(indexPath, new SingleInstanceLockFactory())) {

            while (!stopped) {
                try (SqlSession sqlSession = ThreadLocalSqlSession.getNewReuseSession(TransactionIsolationLevel.READ_COMMITTED)) {
                    IndexWriterConfig writerConfig = new IndexWriterConfig(standardAnalyzer);
                    writerConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
                    writerConfig.setCommitOnClose(true);
                    indexWriter = new IndexWriter(indexDir, writerConfig);

                    Set<IndexKey>   seen       = new HashSet<>(128);
                    IndexJobDao     jobDao     = new IndexJobDao().setSqlSession(sqlSession);
                    List<IndexItem> indexItems = new IndexItemDao().setSqlSession(sqlSession).list();

                    while (jobDao.countJobs() > 0) {
                        List<IndexJob> jobs = jobDao.getIndexJobsByFailedCountWithLimit(0, limit);
                        log.debug("Found " + jobs.size() + " IndexJobs.");
                        if (jobs.size() == 0) {
                            break;
                        }
                        for (IndexJob job : jobs) {
                            IndexKey indexKey = new IndexKey(job.getJobType(), job.getItemId());
                            if (seen.contains(indexKey)) {
                                // remove duplicate jobs in the current transaction
                                continue;
                            }

                            try {
                                switch (job.getAction()) {
                                    case DELETE -> deleteFromIndex(indexKey);
                                    case CREATE, UPDATE -> handleIndexItem(job, indexKey, indexItems);
                                }
                                jobDao.delete(job);
                            } catch (Exception e) {
                                log.info("IndexJob failed with: " + e);
                                job.setFailed(job.getFailed() + 1);
                                jobDao.updateStatus(job);
                            }
                            seen.add(indexKey);
                        }
                        jobDao.commit();
                        long sequenceNr = indexWriter.commit();
                        log.debug("sequenceNr: " + sequenceNr);
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
                    isInitialized = true;
                }
            }
        } catch (Exception e) {
            log.error("Lucene Index Loop failed: ", e);
        }


    }

    private void handleIndexItem(IndexJob job, IndexKey key, List<IndexItem> indexItems) throws IOException {
        Document doc = new Document();
        doc.add(new StoredField(LUCENE_FIELD_UNIQUE_ID, key.toString()));
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

    private boolean indexFolder(Long id, Document doc, List<IndexItem> indexItems) {
        Optional<Folder> folderOpt = folderDao.getFolderById(id);
        if (folderOpt.isEmpty()) {
            return false;
        }
        Folder folder = folderOpt.get();
        //// index sysMeta

        // folderpath
        List<Folder> folders = folderDao.getFolderByIdWithAncestors(folder.getParentId(), false);
        doc.add(new StoredField("folderpath", foldersToPath(folders)));
        doc.add(new NumericDocValuesField("id", folder.getId()));

        return true;
    }

    private boolean indexOsd(Long id, Document doc, List<IndexItem> indexItems) {
        Optional<ObjectSystemData> osdOpt = osdDao.getObjectById(id);
        if (osdOpt.isEmpty()) {
            return false;
        }
        ObjectSystemData osd = osdOpt.get();

        //// index sysMeta

        // folderpath
        List<Folder> folders = folderDao.getFolderByIdWithAncestors(osd.getParentId(), false);
        doc.add(new StoredField("folderpath", foldersToPath(folders)));
        doc.add(new NumericDocValuesField("id", osd.getId()));

        // index content

        // index metasets
        return true;
    }

    private String foldersToPath(List<Folder> folders) {
        List<String> names = folders.stream().map(Folder::getName).toList();
        String       path  = String.join("/", names);
        log.info("folderPath: " + path);
        return path;
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
}
