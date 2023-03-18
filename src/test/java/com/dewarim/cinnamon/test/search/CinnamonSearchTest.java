package com.dewarim.cinnamon.test.search;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.queryparser.flexible.standard.config.PointsConfig;
import org.apache.lucene.queryparser.xml.CoreParser;
import org.apache.lucene.queryparser.xml.ParserException;
import org.apache.lucene.queryparser.xml.builders.ExacPointQueryBuilder;
import org.apache.lucene.queryparser.xml.builders.PointRangeQueryBuilder;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A test class for exploring Apache Lucene indexing and searching - goal is to have a tight feedback loop when
 * debugging "no object found by search" and other search problems.
 */
public class CinnamonSearchTest {

    private static final Logger log = LogManager.getLogger(CinnamonSearchTest.class);


    private Directory memoryIndex = new ByteBuffersDirectory();

    private final String exactPoint = """
            <BooleanQuery><Clause occurs='must'><ExactPointQuery fieldName='p' value='300' type='long'/></Clause></BooleanQuery>
            """;

    private final String pointRangeQuery = """
            <BooleanQuery><Clause occurs='must'><PointRangeQuery fieldName='p' lowerTerm='300' upperTerm='300' type='long'/></Clause></BooleanQuery>
            """;

    @Test
    public void longPointIndexing() throws IOException, QueryNodeException, ParserException {

        try (IndexWriter indexWriter = createIndex()) {
            Document doc = createDocumentWithLongPoint();
            indexWriter.addDocument(doc);


        }
        IndexSearcher indexSearcher = createIndexSearcher();
        searchWithNormalQuery(indexSearcher);
        searchWithXmlExactPointQuery(indexSearcher);
        searchWithXmlPointRangeQuery(indexSearcher);
    }

    private void searchWithXmlPointRangeQuery(IndexSearcher searcher) throws ParserException, IOException {
        InputStream xmlInputStream = new ByteArrayInputStream(pointRangeQuery.getBytes(StandardCharsets.UTF_8));
        CoreParser  coreParser     = new CoreParser("p", new StandardAnalyzer());
        coreParser.addQueryBuilder("PointRangeQuery", new PointRangeQueryBuilder());
        Query query = coreParser.parse(xmlInputStream);
        log.info("query: "+query);
        TopDocs topDocs = searcher.search(query, 100);
        assertEquals(1, topDocs.totalHits.value);
    }
    private void searchWithXmlExactPointQuery(IndexSearcher searcher) throws ParserException, IOException {
        InputStream xmlInputStream = new ByteArrayInputStream(exactPoint.getBytes(StandardCharsets.UTF_8));
        CoreParser  coreParser     = new CoreParser("p", new StandardAnalyzer());
        coreParser.addQueryBuilder("ExactPointQuery", new ExacPointQueryBuilder());
        Query query = coreParser.parse(xmlInputStream);
        log.info("query: "+query);
        TopDocs topDocs = searcher.search(query, 100);
        assertEquals(1, topDocs.totalHits.value);
    }

    private IndexSearcher createIndexSearcher() throws IOException {
        IndexReader indexReader = DirectoryReader.open(memoryIndex);
        return new IndexSearcher(indexReader);
    }

    private void searchWithNormalQuery(IndexSearcher searcher) throws QueryNodeException, IOException {
        PointsConfig              longConfig      = new PointsConfig(new DecimalFormat(), Long.class);
        Map<String, PointsConfig> pointsConfigMap = new HashMap<>();
        pointsConfigMap.put("p", longConfig);
        StandardQueryParser queryParser = new StandardQueryParser(new StandardAnalyzer());
        queryParser.setPointsConfigMap(pointsConfigMap);
        Query   query   = queryParser.parse("300", "p");
        TopDocs topDocs = searcher.search(query, 100);
        assertEquals(1, topDocs.totalHits.value);
    }


    private Document createDocumentWithLongPoint() {
        Document doc = new Document();
        doc.add(new LongPoint("p", 300L));
        return doc;
    }

    private IndexWriter createIndex() throws IOException {
        StandardAnalyzer  analyzer          = new StandardAnalyzer();
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        return new IndexWriter(memoryIndex, indexWriterConfig);
    }


}
