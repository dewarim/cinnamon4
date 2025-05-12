package com.dewarim.cinnamon.application.service;

import com.dewarim.cinnamon.api.content.ContentProvider;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.application.exception.CinnamonException;
import com.dewarim.cinnamon.application.service.index.ContentContainer;
import com.dewarim.cinnamon.configuration.LuceneConfig;
import com.dewarim.cinnamon.dao.*;
import com.dewarim.cinnamon.model.*;
import com.dewarim.cinnamon.model.index.*;
import com.dewarim.cinnamon.model.relations.Relation;
import com.dewarim.cinnamon.provider.ContentProviderService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.dewarim.cinnamon.api.Constants.LUCENE_FIELD_CINNAMON_CLASS;
import static com.dewarim.cinnamon.api.Constants.LUCENE_FIELD_UNIQUE_ID;

public class IndexService implements Runnable {
    private static final Logger log = LogManager.getLogger(IndexService.class);

    public static final byte[] NO_CONTENT = new byte[0];

    public static boolean                isInitialized = false;
    private       boolean                stopped       = false;
    private final LuceneConfig           config;
    private final Path                   indexPath;
    private final XmlMapper              xmlMapper;
    private final List<IndexItem>        indexItems;
    private final ContentProviderService contentProviderService;

    private static final IndexEvent SUCCESS_EVENT = new IndexEvent().setIndexResult(IndexResult.SUCCESS);
    private static final IndexEvent IGNORE_EVENT  = new IndexEvent().setIndexResult(IndexResult.IGNORE);

    public IndexService(LuceneConfig config, ContentProviderService contentProviderService) {
        this.config = config;
        indexPath   = Paths.get(config.getIndexPath());
        if (!indexPath.toFile().exists()) {
            boolean madeDirs = indexPath.toFile().mkdirs();
            if (madeDirs) {
                throw new IllegalStateException("Could not create path to index: " + indexPath.toAbsolutePath());
            }
        }
        indexItems                  = new IndexItemDao().list();
        xmlMapper                   = new XmlMapper();
        this.contentProviderService = contentProviderService;
    }


    public void run() {
        log.info("IndexService thread is running");
        try (Analyzer standardAnalyzer = new StandardAnalyzer();
             Directory indexDir = FSDirectory.open(indexPath, new SingleInstanceLockFactory())) {
            if (!isInitialized) {
                // signals SearchService it's okay to open the (potentially new) index for reading
                initializeLuceneIndex(indexDir, standardAnalyzer);
            }
            while (!stopped) {
                int               maxJobSize   = config.getMaxBatchSize();
                IndexWriterConfig writerConfig = new IndexWriterConfig(standardAnalyzer);
                writerConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
                writerConfig.setCommitOnClose(false);
                try (IndexWriter indexWriter = new IndexWriter(indexDir, writerConfig)) {
                    List<IndexJob> jobs;
                    try (SqlSession sqlSession = ThreadLocalSqlSession.getNewSession(TransactionIsolationLevel.READ_COMMITTED)) {
                        IndexJobDao jobDao = new IndexJobDao(sqlSession);
                        jobs = jobDao.getIndexJobsByFailedCountWithLimit(config.getMaxIndexAttempts(), maxJobSize);
                    }
                    if (jobs.isEmpty()) {
                        log.trace("Found 0 IndexJobs.");
                    }
                    else {
                        log.debug("Found {} IndexJobs with not more than {} failed attempts.", jobs.size(), config.getMaxIndexAttempts());
                        ConcurrentLinkedQueue<IndexJob> jobsToDelete  = new ConcurrentLinkedQueue<>();
                        ConcurrentLinkedQueue<IndexJob> jobsToUpdate  = new ConcurrentLinkedQueue<>();
                        AtomicInteger                   changeCounter = new AtomicInteger();
                        AtomicBoolean                   error         = new AtomicBoolean(false);

                        log.debug("Loading data for index jobs");
                        List<IndexJobWithDependencies> jobsToDo = findJobs(jobs, jobsToDelete, jobsToUpdate);
                        log.debug("Finished loading data for index jobs");

                        ConcurrentLinkedQueue<IndexEvent> indexEvents = new ConcurrentLinkedQueue<>();
                        Consumer<IndexJobWithDependencies> consumer = jobWithDeps -> {
                            IndexKey    indexKey = jobWithDeps.getIndexKey();
                            IndexJob    job      = jobWithDeps.getIndexJob();
                            IndexResult indexResult;
                            switch (job.getAction()) {
                                case DELETE -> {
                                    IndexEvent event = IndexService.this.deleteFromIndex(indexKey, job, indexWriter);
                                    logEvent(indexEvents, event);
                                    indexResult = event.getIndexResult();
                                }
                                case CREATE, UPDATE -> {
                                    try {
                                        IndexEvent indexEvent = IndexService.this.handleIndexItem(jobWithDeps, indexKey, indexItems, indexWriter);
                                        logEvent(indexEvents, indexEvent);
                                        indexResult = indexEvent.getIndexResult();
                                    } catch (Exception e) {
                                        log.error("Failed to handle IndexItem: {}", indexKey, e);
                                        IndexEvent indexEvent = new IndexEvent(job.getId(), IndexEventType.LUCENE, IndexResult.ERROR, e.getMessage());
                                        logEvent(indexEvents, indexEvent);
                                        indexResult = IndexResult.ERROR;
                                    }
                                }
                                // should never happen, all enum fields are covered:
                                default -> indexResult = IndexResult.IGNORE;
                            }

                            switch (indexResult) {
                                case ERROR -> {
                                    error.set(true);
                                    job.setFailed(job.getFailed() + 1);
                                    jobsToUpdate.add(job);
                                }
                                case SUCCESS -> {
                                    jobsToDelete.add(job);
                                    changeCounter.incrementAndGet();
                                }
                                case FAILED -> {
                                    job.setFailed(job.getFailed() + 1);
                                    jobsToUpdate.add(job);
                                }
                                case IGNORE -> {
                                }
                            }
                        };

                        // prevent a CREATE index job to run after a delete or update index job due to concurrency
                        List<Runnable> createJobs = new ArrayList<>();
                        List<Runnable> updateJobs = new ArrayList<>();
                        List<Runnable> deleteJobs = new ArrayList<>();
                        for (IndexJobWithDependencies job : jobsToDo) {
                            Runnable runnable = () -> consumer.accept(job);
                            switch (job.getIndexJob().getAction()) {
                                case CREATE -> createJobs.add(runnable);
                                case UPDATE -> updateJobs.add(runnable);
                                case DELETE -> deleteJobs.add(runnable);
                            }
                        }
                        executeJobs(createJobs);
                        executeJobs(updateJobs);
                        executeJobs(deleteJobs);

                        if (!indexEvents.isEmpty()) {
                            log.info("Failed index events count: {}", indexEvents.size());
                            try (SqlSession sqlSession = ThreadLocalSqlSession.getNewSession(TransactionIsolationLevel.READ_COMMITTED)) {
                                IndexEventDao indexEventDao = new IndexEventDao(sqlSession);
                                try {
                                    indexEventDao.create(indexEvents.stream().toList());
                                    indexEventDao.commit();
                                } catch (Exception e) {
                                    log.error("Failed to save index events: {}", indexEvents, e);
                                    throw e;

                                }
                            }
                        }

                        if (error.get()) {
                            log.error("Error during indexing; trying to roll back indexWriter.");
                            indexWriter.rollback();
                            log.error("Also trying to mark jobs as failed after index error");
                            List<IndexJob> allJobs = new ArrayList<>(jobsToUpdate);
                            allJobs.addAll(jobsToDelete);
                            try (SqlSession sqlSession = ThreadLocalSqlSession.getNewSession(TransactionIsolationLevel.READ_COMMITTED)) {
                                IndexJobDao jobDao = new IndexJobDao(sqlSession);
                                allJobs.forEach(jobDao::updateStatus);
                                jobDao.commit();
                            }
                            log.error("... trying to continue with indexing.");
                            continue;
                        }

                        if (changeCounter.get() > 0) {
                            log.debug("Commit changes to Lucene Index");
                            long sequenceNr = indexWriter.commit();
                            log.info("Committed changes to Lucene index, sequenceNr: {} changeCounter: {}", sequenceNr, changeCounter.get());
                            changeCounter.set(0);
                        }
                        else {
                            log.debug("no change to index");
                        }

                        if (!jobsToDelete.isEmpty()) {
                            log.debug("deleting {} index jobs", jobsToDelete.size());
                            try (SqlSession sqlSession = ThreadLocalSqlSession.getNewSession(TransactionIsolationLevel.READ_COMMITTED)) {
                                IndexJobDao jobDao = new IndexJobDao(sqlSession);
                                jobsToDelete.forEach(jobDao::delete);
                                jobDao.commit();
                            }
                        }
                        if (!jobsToUpdate.isEmpty()) {
                            log.debug("updating {} index jobs", jobsToUpdate.size());
                            try (SqlSession sqlSession = ThreadLocalSqlSession.getNewSession(TransactionIsolationLevel.READ_COMMITTED)) {
                                IndexJobDao jobDao = new IndexJobDao(sqlSession);
                                jobsToUpdate.forEach(jobDao::updateStatus);
                                jobDao.commit();
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("Failed to index: ", e);
                    try (SqlSession sqlSession = ThreadLocalSqlSession.getNewSession(TransactionIsolationLevel.READ_COMMITTED)) {
                        StringWriter stringWriter = new StringWriter();
                        PrintWriter  printWriter  = new PrintWriter(stringWriter);
                        e.printStackTrace(printWriter);
                        IndexEvent    indexEvent    = new IndexEvent(0L, IndexEventType.ERROR, IndexResult.FAILED, "Indexing failed with an exception: " + printWriter);
                        IndexEventDao indexEventDao = new IndexEventDao(sqlSession);
                        indexEventDao.create(List.of(indexEvent));
                        indexEventDao.commit();
                    }
                } finally {
                    // sleep after database was updated & index was committed & closed
                    try {
                        // TODO: avoid busy waiting -> use blocking queue or semaphore?
                        log.info("IndexService will sleep for {} ms", config.getMillisToWaitBetweenRuns());
                        Thread.sleep(config.getMillisToWaitBetweenRuns());
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.warn("Index thread was interrupted.");
                    }
                }
            }
        } catch (Exception e) {
            log.error("Lucene Index Loop failed: ", e);
        }


    }

    private void executeJobs(List<Runnable> jobs) {
        if (jobs.isEmpty()) {
            return;
        }
        int             poolSize        = jobs.size();
        ExecutorService executorService = Executors.newFixedThreadPool(Math.min(poolSize, config.getThreadPoolSize()));
        jobs.forEach(executorService::submit);
        executorService.shutdown();
        try {
            boolean terminatedOkay = executorService.awaitTermination(config.getThreadPoolWaitInMinutes(), TimeUnit.MINUTES);
            if (!terminatedOkay) {
                log.error("IndexJobs timed out after {} minutes of waiting.", config.getThreadPoolWaitInMinutes());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void initializeLuceneIndex(Directory indexDir, Analyzer standardAnalyzer) throws IOException {
        IndexWriterConfig writerConfig = new IndexWriterConfig(standardAnalyzer);
        writerConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        writerConfig.setCommitOnClose(true);
        IndexWriter writer = new IndexWriter(indexDir, writerConfig);
        writer.commit();
        writer.close();
        isInitialized = true;
    }

    private void logEvent(ConcurrentLinkedQueue<IndexEvent> indexEvents, IndexEvent event) {
        if (event.getIndexResult() != IndexResult.SUCCESS && event.getIndexResult() != IndexResult.IGNORE) {
            indexEvents.add(event);
        }
    }

    private List<IndexJobWithDependencies> findJobs(List<IndexJob> jobs,
                                                    ConcurrentLinkedQueue<IndexJob> jobsToDelete,
                                                    ConcurrentLinkedQueue<IndexJob> jobsToUpdate) {
        if (jobs.isEmpty()) {
            return Collections.emptyList();
        }
        Set<IndexKey> seen = new HashSet<>();

        List<IndexJobWithDependencies> jobsToDo    = new ArrayList<>();
        long                           metaSizeSum = 0L;
        try (SqlSession sqlSession = ThreadLocalSqlSession.getNewSession(TransactionIsolationLevel.READ_COMMITTED)) {
            OsdDao        osdDao        = new OsdDao(sqlSession);
            FolderDao     folderDao     = new FolderDao(sqlSession);
            RelationDao   relationDao   = new RelationDao(sqlSession);
            FormatDao     formatDao     = new FormatDao(sqlSession);
            OsdMetaDao    osdMetaDao    = new OsdMetaDao(sqlSession);
            FolderMetaDao folderMetaDao = new FolderMetaDao(sqlSession);

            List<Long> osdsToLoad = jobs.stream().filter(job -> job.getJobType().equals(IndexJobType.OSD)).map(IndexJob::getItemId).toList();
            log.info("Will load {} osds to index.", osdsToLoad.size());
            Map<Long, ObjectSystemData> osds = osdDao.getObjectsById(osdsToLoad, true)
                    .stream().collect(Collectors.toMap(ObjectSystemData::getId, Function.identity()));
            List<Long> foldersToLoad = jobs.stream().filter(job -> job.getJobType().equals(IndexJobType.FOLDER)).map(IndexJob::getItemId).toList();
            log.info("Will load {} folders to index.", foldersToLoad.size());
            Map<Long, Folder> folders = folderDao.getObjectsById(foldersToLoad)
                    .stream().collect(Collectors.toMap(Folder::getId, Function.identity()));
            Set<Long> folderIds = folders.values().stream().map(folder -> {
                if (folder.getParentId() == null) {
                    return folder.getId();
                }
                else {
                    return folder.getParentId();
                }
            }).collect(Collectors.toSet());
            folderIds.addAll(osds.values().stream().map(ObjectSystemData::getParentId).toList());
            log.debug("Determine folderPaths of {} folders", folderIds.size());
            Map<Long, String>     folderPaths = folderDao.getFolderPaths(folderIds.stream().toList());
            Map<Long, List<Meta>> osdMetas    = new HashMap<>();
            for (Meta meta : osdMetaDao.listMetaByObjectIds(osds.keySet().stream().toList())) {
                List<Meta> metas = osdMetas.getOrDefault(meta.getObjectId(), new ArrayList<>());
                metas.add(meta);
                osdMetas.put(meta.getObjectId(), metas);
            }

            for (IndexJob indexJob : jobs) {
                IndexKey indexKey = new IndexKey(indexJob.getJobType(), indexJob.getItemId(), indexJob.getAction());
                if (seen.add(indexKey)) {
                    IndexJobWithDependencies indexJobWithDependencies = new IndexJobWithDependencies(indexJob, indexKey);
                    if (indexJob.getJobType().equals(IndexJobType.OSD)) {
                        // OSD:
                        ObjectSystemData osd = osds.get(indexJob.getItemId());
                        if (osd == null) {
                            log.warn("IndexJob: OSD {} not found, skipping.", indexJob.getItemId());
                            jobsToDelete.add(indexJob);
                            continue;
                        }
                        indexJobWithDependencies.setOsd(osd);
                        String folderPath = folderPaths.get(osd.getParentId());
                        log.trace("folderPath: {}", folderPath);
                        indexJobWithDependencies.setFolderPath(folderPath);
                        List<Long>     relationCriteria = List.of(osd.getId());
                        List<Relation> relations        = relationDao.getRelationsOrMode(relationCriteria, relationCriteria, Collections.emptyList(), true);
                        for (Relation relation : relations) {
                            relation.setParent(relation.getLeftId().equals(osd.getId()));
                        }
                        osd.setRelations(relations);
                        Long formatId = osd.getFormatId();
                        if (formatId != null) {
                            Optional<Format> formatOpt = formatDao.getObjectById(formatId);
                            formatOpt.ifPresent(indexJobWithDependencies::setFormat);
                            if (formatOpt.isEmpty()) {
                                log.error("Format {} not found for IndexJob {}!", osd.getFormatId(), indexJob.getId());
                                indexJob.setFailed(indexJob.getFailed() + 1);
                                jobsToUpdate.add(indexJob);
                                continue;
                            }
                            indexJobWithDependencies.setFormat(formatOpt.get());
                        }
                        List<Meta> metas = osdMetas.getOrDefault(osd.getId(), List.of());
                        for (Meta meta : metas) {
                            metaSizeSum += meta.getContent().length();
                        }
                        osd.setMetas(metas);
                        if (metaSizeSum > config.getMaxCombinedMetasetSize()) {
                            log.info("Reached maxCombinedMetasetSize of {}, will skip some elements and take them on next time.", config.getMaxCombinedMetasetSize());
                            break;
                        }
                    }
                    else {
                        // Folder:
                        Folder folder = folders.get(indexJob.getItemId());
                        if (folder == null) {
                            log.warn("IndexJob: Folder {} not found, skipping.", indexJob.getItemId());
                            jobsToDelete.add(indexJob);
                            continue;
                        }
                        String folderPath = folderPaths.get(folder.getParentId());
                        if (folderPath == null) {
                            folderPath = "/";
                        }
                        indexJobWithDependencies.setFolderPath(folderPath);
                        indexJobWithDependencies.setFolder(folder);
                        List<Meta> metas = folderMetaDao.listByFolderId(folder.getId());
                        for (Meta meta : metas) {
                            metaSizeSum += meta.getContent().length();
                        }
                        folder.setMetas(metas);
                        if (metaSizeSum > config.getMaxCombinedMetasetSize()) {
                            log.info("Reached maxCombinedMetasetSize of {}, will skip some elements and take them on next time.", config.getMaxCombinedMetasetSize());
                            break;
                        }
                    }
                    jobsToDo.add(indexJobWithDependencies);
                }
                else {
                    jobsToDelete.add(indexJob);
                }
            }
        }
        return jobsToDo;
    }

    private IndexEvent handleIndexItem(IndexJobWithDependencies jobWithDependencies, IndexKey key, List<IndexItem> indexItems, IndexWriter indexWriter) throws IOException {
        Document doc = new Document();
        doc.add(new StringField(LUCENE_FIELD_UNIQUE_ID, key.toString(), Field.Store.NO));
        doc.add(new Field(LUCENE_FIELD_CINNAMON_CLASS, key.type().toString(), StringField.TYPE_STORED));
        IndexJob   job = jobWithDependencies.getIndexJob();
        IndexEvent indexEvent;
        log.debug("handle IndexJob: {} ", job);
        switch (job.getJobType()) {
            case OSD -> indexEvent = indexOsd(jobWithDependencies, doc, indexItems);
            case FOLDER -> indexEvent = indexFolder(jobWithDependencies, doc, indexItems);
            default -> indexEvent = IGNORE_EVENT;
        }
        if (!indexWriter.isOpen()) {
            log.warn("IndexWriter is closed!");
        }
        switch (indexEvent.getIndexResult()) {
            case FAILED -> log.info("Failed to index {}", job);
            case SUCCESS -> {
                switch (job.getAction()) {
                    case UPDATE -> indexWriter.updateDocument(new Term(LUCENE_FIELD_UNIQUE_ID, key.toString()), doc);
                    case CREATE -> indexWriter.addDocument(doc);
                }
            }
            default -> {
            }
        }
        return indexEvent;
    }

    private IndexEvent indexFolder(IndexJobWithDependencies job, Document doc, List<IndexItem> indexItems) {
        Folder folder = job.getFolder();
        try {
            //// index sysMeta
            // folderpath
            String folderPath = job.getFolderPath();
            log.debug("folderpath: {}", folderPath);
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
            applyIndexItems(doc, indexItems, xmlMapper.writeValueAsString(folder), NO_CONTENT, folderPath, job);
        } catch (Exception e) {
            log.warn("Failed to index folder #{}", job.getFolder().getId(), e);
            return new IndexEvent(job.getIndexJob().getId(), IndexEventType.GENERIC, IndexResult.FAILED, e.getMessage());
        }
        return SUCCESS_EVENT;
    }

    private IndexEvent indexOsd(IndexJobWithDependencies job, Document doc, List<IndexItem> indexItems) {
        ObjectSystemData osd = job.getOsd();
        try {
            // index sysMeta

            doc.add(new StringField("folderpath", job.getFolderPath().toLowerCase(), Field.Store.NO));

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

            byte[] content = NO_CONTENT;
            if (osd.getContentPath() != null && job.getFormat() != null) {
                ContentProvider contentProvider = contentProviderService.getContentProvider(osd.getContentProvider());
                try (InputStream contentStream = contentProvider.getContentStream(osd)) {
                    Format format = job.getFormat();
                    switch (format.getIndexMode()) {
                        case XML -> content = contentStream.readAllBytes();
                        case JSON -> content = convertJsonToXml(contentStream);
                        case PLAIN_TEXT -> content = ("<plainText>" + new String(contentStream.readAllBytes(), StandardCharsets.UTF_8) + "</plainText>").getBytes();
                    }
                } catch (IOException e) {
                    throw new CinnamonException("Failed to load content for OSD " + osd.getId() + " at " + osd.getContentPath(), e);
                }
            }
            applyIndexItems(doc, indexItems, xmlMapper.writeValueAsString(osd), content, job.getFolderPath(), job);
        } catch (Exception e) {
            log.warn("Indexing failed for OSD #{}", job.getOsd().getId(), e);
            return new IndexEvent(job.getIndexJob().getId(), IndexEventType.GENERIC, IndexResult.FAILED, e.getMessage());
        }
        return SUCCESS_EVENT;
    }

    private byte[] convertJsonToXml(InputStream jsonStream) throws IOException {
        JsonNode jsonNode = new ObjectMapper().readTree(jsonStream);
        byte[]   xml      = new XmlMapper().writeValueAsBytes(jsonNode);
        log.debug("converted json to xml: {}", new String(xml));
        return xml;
    }

    private void applyIndexItems(Document luceneDoc, List<IndexItem> indexItems,
                                 String objectAsString, byte[] content, String folderPath, IndexJobWithDependencies job) {
        ContentContainer   contentContainer = new ContentContainer(objectAsString, content, folderPath, job.getIndexKey().toString());
        org.dom4j.Document xmlDoc           = contentContainer.getCombinedDocument();
//        log.debug("xml doc: {}", xmlDoc.asXML());
        for (IndexItem indexItem : indexItems) {
            String fieldName    = indexItem.getFieldName();
            String searchString = indexItem.getSearchString();
            log.trace("indexing for field: {} with {}", fieldName, searchString);
            // TODO: check search condition, probably with xmlDoc
            if (xmlDoc.valueOf(indexItem.getSearchCondition()).equals("true")) {
                indexItem.getIndexType().getIndexer()
                        .indexObject(xmlDoc, contentContainer.asNode(), luceneDoc, fieldName, searchString, indexItem.isMultipleResults());
            }
            else {
                log.debug("searchCondition failed: {}", indexItem.getSearchCondition());
            }
        }
    }

    private IndexEvent deleteFromIndex(IndexKey key, IndexJob job, IndexWriter indexWriter) {
        try {
            indexWriter.deleteDocuments(new Term(LUCENE_FIELD_UNIQUE_ID, key.toString()));
            return SUCCESS_EVENT;
        } catch (Exception e) {
            log.warn("Failed to delete document from Lucene index: {}", key, e);
            return new IndexEvent(job.getId(), IndexEventType.GENERIC, IndexResult.FAILED, e.getMessage());
        }
    }

    public void setStopped(boolean stopped) {
        this.stopped = stopped;
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
