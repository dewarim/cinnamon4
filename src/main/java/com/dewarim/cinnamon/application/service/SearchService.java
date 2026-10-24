package com.dewarim.cinnamon.application.service;


import com.dewarim.cinnamon.application.DbSessionFactory;
import com.dewarim.cinnamon.application.exception.CinnamonException;
import com.dewarim.cinnamon.application.service.index.CinnamonAnalyzer;
import com.dewarim.cinnamon.application.service.search.BrowsableAcls;
import com.dewarim.cinnamon.application.service.search.RegexQueryBuilder;
import com.dewarim.cinnamon.application.service.search.ResultCollectorManager;
import com.dewarim.cinnamon.application.service.search.WildcardQueryBuilder;
import com.dewarim.cinnamon.configuration.LuceneConfig;
import com.dewarim.cinnamon.dao.FolderDao;
import com.dewarim.cinnamon.dao.IndexJobDao;
import com.dewarim.cinnamon.dao.OsdDao;
import com.dewarim.cinnamon.model.UserAccount;
import com.dewarim.cinnamon.model.index.IndexJob;
import com.dewarim.cinnamon.model.index.IndexJobAction;
import com.dewarim.cinnamon.model.index.IndexJobType;
import com.dewarim.cinnamon.model.index.SearchResult;
import com.dewarim.cinnamon.model.request.search.SearchType;
import com.dewarim.cinnamon.security.authorization.AuthorizationService;
import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import org.apache.ibatis.session.SqlSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.miscellaneous.LimitTokenCountAnalyzer;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.xml.CoreParser;
import org.apache.lucene.queryparser.xml.builders.ExactPointQueryBuilder;
import org.apache.lucene.queryparser.xml.builders.PointRangeQueryBuilder;
import org.apache.lucene.queryparser.xml.builders.RangeQueryBuilder;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;

import static com.dewarim.cinnamon.api.Constants.*;

public class SearchService {

    private static final Logger log = LogManager.getLogger(SearchService.class);

    private final LuceneConfig            config;
    private       DirectoryReader         indexReader;
    private final SearcherManager         searcherManager;
    private final LimitTokenCountAnalyzer limitTokenCountAnalyzer;
    private final RetryPolicy<Boolean>    retryPolicy;
    private final DbSessionFactory        dbSessionFactory;

    public SearchService(LuceneConfig luceneConfig, DbSessionFactory dbLoggingSessionFactory) throws IOException, InterruptedException {
        this.config = luceneConfig;
        this.dbSessionFactory = dbLoggingSessionFactory;

        while (!IndexService.isInitialized) {
            log.debug("Waiting for IndexService to finish initialization.");
            Thread.sleep(1000);
        }
        IndexSearcher.setMaxClauseCount(10000);
        Directory directory = FSDirectory.open(Path.of(config.getIndexPath()));
        indexReader = DirectoryReader.open(directory);
        searcherManager = new SearcherManager(indexReader, new SearcherFactory());
        searcherManager.maybeRefresh();
        // must be the same analyzer IndexService indexes with, or analyzed queries stop matching
        Analyzer analyzer = new CinnamonAnalyzer(config.isCjkOutputUnigrams());
        limitTokenCountAnalyzer = new LimitTokenCountAnalyzer(analyzer, Integer.MAX_VALUE);

        retryPolicy = RetryPolicy.<Boolean>builder()
                .handleResult(false)
                // a permanently failed index job will never succeed, so stop retrying at once
                .abortOn(IndexJobFailedPermanentlyException.class)
                .withDelay(Duration.ofMillis(config.getMillisToWaitBetweenRuns()))
                .withMaxRetries(config.getWaitingRetries())
                .build();

        // TODO: refresh periodically - or if last refresh was > config.lucene.refreshTime
    }

    /**
     * Thrown when an index job has failed so often that IndexService will not pick it up again, so
     * there is no point in waiting for it any longer.
     */
    private static class IndexJobFailedPermanentlyException extends RuntimeException {
        IndexJobFailedPermanentlyException(String message) {
            super(message);
        }
    }

    /**
     * Wait until the items of the given index jobs are searchable (used when waitUntilSearchable is
     * enabled).
     * <p>
     * Note that this cannot fail the request: the data has already been committed, so reporting a
     * failure to the client would be worse than reporting success - the object does exist, it is just
     * not searchable yet. Both ways of not reaching the goal are therefore logged at error level
     * rather than thrown, and they are logged distinguishably, because previously a permanently
     * failed job was indistinguishable from a successful wait.
     */
    public void waitUntilIndexed(List<IndexJob> indexJobs) {
        log.debug("waitUntilIndexed for {} jobs", indexJobs.size());
        List<IndexJob> jobsToWaitFor = indexJobs.stream()
                .filter(job -> job.getAction() == IndexJobAction.CREATE || job.getAction() == IndexJobAction.UPDATE)
                .toList();
        IndexJobDao indexJobDao = new IndexJobDao();
        for (IndexJob job : jobsToWaitFor) {
            log.debug("Waiting for job: {}", job);
            try {

                Failsafe.with(retryPolicy).run(() -> {
                    Optional<IndexJob> indexJob;
                    try (SqlSession sqlSession = dbSessionFactory.getSession()) {
                        indexJobDao.setSqlSession(sqlSession);
                        indexJob = indexJobDao.getIndexJobById(job.getId());
                    }
                    if (indexJob.isPresent()) {
                        IndexJob currentJob = indexJob.get();
                        // IndexService selects jobs with "failed <= maxIndexAttempts", so a job is
                        // only abandoned once it has failed MORE often than that. Giving up at
                        // ">= maxIndexAttempts" stopped waiting one attempt too early.
                        if (currentJob.getFailed() > config.getMaxIndexAttempts()) {
                            throw new IndexJobFailedPermanentlyException("index job " + currentJob.getId()
                                    + " failed " + currentJob.getFailed() + " times, more than the configured "
                                    + config.getMaxIndexAttempts() + " maxIndexAttempts");
                        }
                        log.debug("Job {} still pending (failed: {})", currentJob.getId(), currentJob.getFailed());
                        throw new CinnamonException("Job still pending");
                    }

                    log.debug("Job {} finished, checking Lucene", job.getId());
                    if (!isIndexed(job.getJobType(), job.getItemId())) {
                        log.debug("Item {} not yet in Lucene", job.getItemId());
                        throw new CinnamonException("Job finished but item not yet searchable");
                    }
                    log.debug("Item {} is indexed.", job.getItemId());
                });
            } catch (IndexJobFailedPermanentlyException e) {
                log.error("Item {} ({}) will NOT become searchable: {}. Indexing has given up on it.",
                        job.getItemId(), job.getJobType(), e.getMessage());
            } catch (Exception e) {
                log.error("Gave up waiting for item {} ({}) to become searchable after {} retries ({}ms apart):" +
                                " {}. The item exists, but is not searchable yet.",
                        job.getItemId(), job.getJobType(), config.getWaitingRetries(),
                        config.getMillisToWaitBetweenRuns(), e.getMessage());
            }
        }
    }

    private boolean isIndexed(IndexJobType type, Long id) throws IOException {
        IndexSearcher searcher = null;
        try {
            searcherManager.maybeRefreshBlocking();
            searcher = searcherManager.acquire();
            BooleanQuery query = new BooleanQuery.Builder()
                    .add(new TermQuery(new Term(LUCENE_FIELD_CINNAMON_CLASS, type.name())), BooleanClause.Occur.MUST)
                    .add(LongPoint.newExactQuery("id", id), BooleanClause.Occur.MUST)
                    .build();
            TopDocs topDocs = searcher.search(query, 1);
            boolean found   = topDocs.totalHits.value > 0;
            if (!found) {
                log.debug("Item {} of type {} not yet indexed.", id, type);
            }
            return found;
        } finally {
            if (searcher != null) {
                searcherManager.release(searcher);
            }
        }
    }

    public IndexJobDao.IndexRows countDocs() throws IOException {
        IndexSearcher searcher = null;
        try {
            if (!searcherManager.isSearcherCurrent()) {
                searcherManager.maybeRefreshBlocking();
            }
            searcher = searcherManager.acquire();
            int documents = searcher.count(new TermQuery(new Term(LUCENE_FIELD_CINNAMON_CLASS, "OSD")));
            int folders   = searcher.count(new TermQuery(new Term(LUCENE_FIELD_CINNAMON_CLASS, "FOLDER")));
            return new IndexJobDao.IndexRows(documents, folders);
        } catch (Exception e) {
            log.warn("countDocs failed: ", e);
            throw new CinnamonException("countDocs failed", e);
        } finally {
            if (searcher != null) {
                searcherManager.release(searcher);
            }
        }
    }

    public SearchResult doSearch(String xmlQuery, SearchType searchType, UserAccount user) throws IOException {
        IndexSearcher searcher = null;
        try {
            if (!searcherManager.isSearcherCurrent()) {
                searcherManager.maybeRefreshBlocking();
            }
            searcher = searcherManager.acquire();
            log.debug("xmlQuery: {}", xmlQuery);
            InputStream xmlInputStream = new ByteArrayInputStream(xmlQuery.getBytes(StandardCharsets.UTF_8));
            CoreParser  coreParser     = new CoreParser("content", limitTokenCountAnalyzer);
            coreParser.addQueryBuilder("WildcardQuery", new WildcardQueryBuilder());
            coreParser.addQueryBuilder("RegexQuery", new RegexQueryBuilder());
            coreParser.addQueryBuilder("PointRangeQuery", new PointRangeQueryBuilder());
            coreParser.addQueryBuilder("RangeQuery", new RangeQueryBuilder());
            coreParser.addQueryBuilder("ExactPointQuery", new ExactPointQueryBuilder());
            Query query;
            try {
                query = coreParser.parse(xmlInputStream);
            } catch (StackOverflowError e) {
                // Lucene's recursive descent parsers (RegExp, classic QueryParser) overflow on deeply nested input.
                // Turn this into a regular exception so it fails like any other unparseable query.
                throw new CinnamonException("query is nested too deeply", e);
            }
            log.debug("parsed query: {}", query);

            // Determine the user's browse permissions *before* searching and push them into the query,
            // so documents the user may not browse are never collected.
            BrowsableAcls browsableAcls = new AuthorizationService().getBrowsableAcls(user);
            Query         browseFilter  = browsePermissionFilter(browsableAcls);
            List<Long>    osdIds        = List.of();
            List<Long>    folderIds     = List.of();
            if (browseFilter != null) {
                switch (searchType) {
                    case OSD -> osdIds = searchForType(searcher, query, browseFilter, IndexJobType.OSD);
                    case FOLDER -> folderIds = searchForType(searcher, query, browseFilter, IndexJobType.FOLDER);
                    case ALL -> {
                        osdIds = searchForType(searcher, query, browseFilter, IndexJobType.OSD);
                        folderIds = searchForType(searcher, query, browseFilter, IndexJobType.FOLDER);
                    }
                }
            } else {
                log.debug("User {} has no browsable acls, returning empty result.", user.getId());
            }

            // see #414 - search finds objects that have been deleted
            // not sure why - though the indexing process is asynchronous and can take a couple of seconds to catch up,
            // so maybe it's a case of "delete - re-index in progress - search before Lucene is finished"
            // added logging to help debug this further & to detect inconsistencies in the results.
            if (config.isVerifySearchResults()) {
                OsdDao    osdDao       = new OsdDao();
                Set<Long> luceneOsdIds = new HashSet<>(osdIds);
                Set<Long> knownOsdIds  = new HashSet<>(osdDao.findKnownIds(osdIds));
                if (knownOsdIds.size() != luceneOsdIds.size()) {
                    log.warn("Lucene result list contains {} unknown OSD ids", luceneOsdIds.size() - knownOsdIds.size());
                    luceneOsdIds.removeAll(knownOsdIds);
                    log.warn("Unknown OSD ids: {} - will enqueue them for removal from index.", luceneOsdIds);
                    // enqueue delete-index jobs for unknown OSD ids found in Lucene
                    IndexJobDao indexJobDao = new IndexJobDao();
                    for (Long unknownOsdId : luceneOsdIds) {
                        IndexJob deleteJob = new IndexJob(IndexJobType.OSD, unknownOsdId, IndexJobAction.DELETE);
                        indexJobDao.insertIndexJob(deleteJob, false);
                    }
                }
                FolderDao folderDao       = new FolderDao();
                Set<Long> luceneFolderIds = new HashSet<>(folderIds);
                Set<Long> knownFolderIds  = new HashSet<>(folderDao.findKnownIds(folderIds));
                if (knownFolderIds.size() != folderIds.size()) {
                    log.warn("Lucene result list contains {} unknown Folder ids", luceneFolderIds.size() - knownFolderIds.size());
                    luceneFolderIds.removeAll(knownFolderIds);
                    log.warn("Unknown Folder ids: {} - will enqueue them for removal from index.", luceneFolderIds);
                    IndexJobDao indexJobDao = new IndexJobDao();
                    for (Long unknownFolderId : luceneFolderIds) {
                        IndexJob deleteJob = new IndexJob(IndexJobType.FOLDER, unknownFolderId, IndexJobAction.DELETE);
                        indexJobDao.insertIndexJob(deleteJob, false);
                    }
                }
                return new SearchResult(new ArrayList<>(knownOsdIds), new ArrayList<>(knownFolderIds));
            }

            return new SearchResult(osdIds, folderIds);
        } catch (Exception e) {
            log.warn("search failed: ", e);
            throw new CinnamonException("search failed", e);
        } finally {
            if (searcher != null) {
                searcherManager.release(searcher);
            }
        }
    }

    /**
     * Run the user's query restricted to one Cinnamon type, with the browse-permission filter applied
     * by Lucene itself. Only the ids of matching documents are collected.
     */
    private List<Long> searchForType(IndexSearcher searcher, Query userQuery, Query browseFilter,
                                     IndexJobType jobType) throws IOException {
        Query query = new BooleanQuery.Builder()
                .add(userQuery, BooleanClause.Occur.MUST)
                .add(new TermQuery(new Term(LUCENE_FIELD_CINNAMON_CLASS, jobType.name())), BooleanClause.Occur.FILTER)
                .add(browseFilter, BooleanClause.Occur.FILTER)
                .build();
        long[] ids = searcher.search(query, new ResultCollectorManager());
        log.debug("Found {} {} documents.", ids.length, jobType);
        List<Long> result = new ArrayList<>(ids.length);
        for (long id : ids) {
            result.add(id);
        }
        return result;
    }

    /**
     * Translates {@link BrowsableAcls#hasBrowsePermission} into a Lucene query:
     * <pre>(acl IN objectAcls) OR (owner == userId AND acl IN ownerAcls)</pre>
     * Note that {@code LongPoint.newSetQuery} builds a single PointInSetQuery rather than one boolean
     * clause per value, so even a large number of acls does not count towards maxClauseCount.
     *
     * @return the filter, or null if the user cannot browse anything at all (in which case no search
     * needs to run)
     */
    // package private & static for SearchServiceUnitTest, which verifies it against hasBrowsePermission
    static Query browsePermissionFilter(BrowsableAcls browsableAcls) {
        boolean hasObjectAcls = !browsableAcls.objectAcls().isEmpty();
        boolean hasOwnerAcls  = !browsableAcls.ownerAcls().isEmpty() && browsableAcls.userId() != null;
        if (!hasObjectAcls && !hasOwnerAcls) {
            return null;
        }
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        if (hasObjectAcls) {
            builder.add(LongPoint.newSetQuery(LUCENE_FIELD_ACL_ID, browsableAcls.objectAcls()),
                    BooleanClause.Occur.SHOULD);
        }
        if (hasOwnerAcls) {
            Query ownedByUserWithBrowsableAcl = new BooleanQuery.Builder()
                    .add(LongPoint.newExactQuery(LUCENE_FIELD_OWNER_ID, browsableAcls.userId()), BooleanClause.Occur.MUST)
                    .add(LongPoint.newSetQuery(LUCENE_FIELD_ACL_ID, browsableAcls.ownerAcls()), BooleanClause.Occur.MUST)
                    .build();
            builder.add(ownedByUserWithBrowsableAcl, BooleanClause.Occur.SHOULD);
        }
        return builder.setMinimumNumberShouldMatch(1).build();
    }

}
