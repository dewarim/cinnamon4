package com.dewarim.cinnamon.application.service;


import com.dewarim.cinnamon.application.exception.CinnamonException;
import com.dewarim.cinnamon.application.service.search.BrowsableAcls;
import com.dewarim.cinnamon.application.service.search.RegexQueryBuilder;
import com.dewarim.cinnamon.application.service.search.ResultCollector;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.miscellaneous.LimitTokenCountAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.dewarim.cinnamon.api.Constants.*;

public class SearchService {

    private static final Logger log = LogManager.getLogger(SearchService.class);

    private final LuceneConfig            config;
    private       DirectoryReader         indexReader;
    private final SearcherManager         searcherManager;
    private final LimitTokenCountAnalyzer limitTokenCountAnalyzer;

    public SearchService(LuceneConfig luceneConfig) throws IOException, InterruptedException {
        this.config = luceneConfig;

        while (!IndexService.isInitialized) {
            log.debug("Waiting for IndexService to finish initialization.");
            Thread.sleep(1000);
        }
        IndexSearcher.setMaxClauseCount(10000);
        Directory directory = FSDirectory.open(Path.of(config.getIndexPath()));
        indexReader = DirectoryReader.open(directory);
        searcherManager = new SearcherManager(indexReader, new SearcherFactory());
        searcherManager.maybeRefresh();
        Analyzer analyzer = new StandardAnalyzer();
        limitTokenCountAnalyzer = new LimitTokenCountAnalyzer(analyzer, Integer.MAX_VALUE);

        // TODO: refresh periodically - or if last refresh was > config.lucene.refreshTime
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
            Query query = coreParser.parse(xmlInputStream);
            log.debug("parsed query: {}", query);
            ResultCollector collector = new ResultCollector(searcher);
            searcher.search(query, collector);
            log.debug("Found {} documents.", collector.getDocuments().size());

            BrowsableAcls browsableAcls = new AuthorizationService().getBrowsableAcls(user);
            List<Long>    osdIds        = List.of();
            List<Long>    folderIds     = List.of();
            switch (searchType) {
                case OSD -> osdIds = filterForType(collector, IndexJobType.OSD, browsableAcls);
                case FOLDER -> folderIds = filterForType(collector, IndexJobType.FOLDER, browsableAcls);
                case ALL -> {
                    osdIds = filterForType(collector, IndexJobType.OSD, browsableAcls);
                    folderIds = filterForType(collector, IndexJobType.FOLDER, browsableAcls);
                }
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
                    for (Long unknownOsdId : luceneOsdIds) {
                        IndexJob deleteJob = new IndexJob(IndexJobType.FOLDER, unknownOsdId, IndexJobAction.DELETE);
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

    private List<Long> filterForType(ResultCollector collector, IndexJobType jobType, BrowsableAcls browsableAcls) {
        return collector.getDocuments().stream()
                .filter(doc -> doc.getField(LUCENE_FIELD_CINNAMON_CLASS).stringValue().equals(jobType.name()))
                .filter(doc -> browsableAcls.hasBrowsePermission(
                        doc.getField(LUCENE_FIELD_ACL_ID).numericValue().longValue(),
                        doc.getField(LUCENE_FIELD_OWNER_ID).numericValue().longValue()))
                .map(doc -> doc.getField("id").numericValue().longValue())
                .collect(Collectors.toSet())
                .stream()
                .toList();
    }

}
