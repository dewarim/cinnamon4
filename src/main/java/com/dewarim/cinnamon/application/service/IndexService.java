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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.StoredField;
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

public class IndexService implements Runnable {
    private static final Logger log = LogManager.getLogger(IndexService.class);

    private       boolean     stopped   = false;
    private final IndexWriter indexWriter;
    private final OsdDao      osdDao    = new OsdDao();
    private final FolderDao   folderDao = new FolderDao();
    private final IndexJobDao jobDao    = new IndexJobDao();

    public IndexService(LuceneConfig config) {

        Path indexPath = Paths.get(config.getIndexPath());
        if (!indexPath.toFile().exists()) {
            boolean madeDirs = indexPath.toFile().mkdirs();
            if (madeDirs) {
                throw new IllegalStateException("Could not create path to index: " + indexPath.toAbsolutePath());
            }
        }
        try (Analyzer standardAnalyzer = new StandardAnalyzer(); Directory indexDir = FSDirectory.open(indexPath, new SingleInstanceLockFactory())) {
            IndexWriterConfig writerConfig = new IndexWriterConfig(standardAnalyzer);
            writerConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            writerConfig.setCommitOnClose(true);
            indexWriter = new IndexWriter(indexDir, writerConfig);
            indexWriter.commit();
        } catch (IOException e) {
            log.debug("failed to initialize lucene for repository $name", e);
            throw new RuntimeException("error.lucene.IO", e);
        }
    }


    public void run() {
        int limit = 100;
        while (!stopped) {
            try {
                ThreadLocalSqlSession.refreshSession();
                List<IndexItem> indexItems = new IndexItemDao().list();
                Set<IndexKey>   seen       = new HashSet<>(128);
                while (jobDao.countJobs() > 0) {

                    List<IndexJob> jobs = jobDao.getIndexJobsByFailedCountWithLimit(0, limit);
                    log.debug("Found " + jobs.size() + " IndexJobs.");
                    for (IndexJob job : jobs) {
                        IndexKey indexKey = new IndexKey(job.getJobType(), job.getItemId());
                        if (seen.contains(indexKey)) {
                            // remove duplicate jobs in the current transaction
                            continue;
                        }

                        try {
                            switch (job.getAction()) {
                                case DELETE -> deleteFromIndex(indexKey);
                                case CREATE -> createIndexItem(job, indexKey, indexItems);
                                case UPDATE -> updateIndexItem(job, indexKey, indexItems);
                            }
                            jobDao.delete(job);
                        } catch (Exception e) {
                            job.setFailed(job.getFailed() + 1);
                            jobDao.updateStatus(job);
                        }
                        seen.add(indexKey);
                    }
                    jobDao.commit();
                    // TODO: avoid busy waiting
                    Thread.sleep(1000L);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Index thread was interrupted.");
            } catch (Exception e) {
                log.warn("Failed to index: ", e);
            }
        }
    }

    private void updateIndexItem(IndexJob job, IndexKey key, List<IndexItem> indexItems) {
    }

    private void createIndexItem(IndexJob job, IndexKey key, List<IndexItem> indexItems) throws IOException {
        Document doc = new Document();
        doc.add(new StoredField("uniqueId", key.toString()));
        boolean failed = false;
        switch (job.getJobType()) {
            case OSD -> failed = indexOsd(job.getItemId(), doc, indexItems);
            case FOLDER -> failed = indexFolder(job.getItemId(), doc, indexItems);
        }
        if (failed) {
            return;
        }
        indexWriter.addDocument(doc);
    }

    private boolean indexFolder(Long id, Document doc, List<IndexItem> indexItems) {
        Optional<Folder> folderOpt = folderDao.getFolderById(id);
        if (folderOpt.isEmpty()) {
            return false;
        }
        Folder folder = folderOpt.get();
        // index sysMeta
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
        List<Folder> folders = folderDao.getFolderByIdWithAncestors(osd.getId(), false);
        doc.add(new StoredField("folderpath", foldersToPath(folders)));
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
        indexWriter.deleteDocuments(new Term("uniqueId", key.toString()));
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


}
