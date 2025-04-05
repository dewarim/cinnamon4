package com.dewarim.cinnamon.test.search;

import com.dewarim.cinnamon.application.service.index.ContentContainer;
import com.dewarim.cinnamon.application.service.index.DescendingStringIndexer;
import com.dewarim.cinnamon.application.service.search.WildcardQueryBuilder;
import com.dewarim.cinnamon.model.Meta;
import com.dewarim.cinnamon.model.ObjectSystemData;
import com.dewarim.cinnamon.model.relations.Relation;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
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
import org.apache.lucene.queryparser.xml.builders.ExactPointQueryBuilder;
import org.apache.lucene.queryparser.xml.builders.PointRangeQueryBuilder;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.List;
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
    private final String wildcardQuery = """
            <?xml version="1.0" encoding="utf-8"?>
            <BooleanQuery>
              <Clause occurs="must">
                <WildcardQuery fieldName="content">__queryString__</WildcardQuery>
              </Clause>
            </BooleanQuery>
            """;
    private static ObjectSystemData osd;

    @BeforeAll
    public static void setup(){
        osd = new ObjectSystemData();
        Meta meta = new Meta(1L,1L,"<html><body><div><p>Schnitzel und Verbrechen</p></div></body></html>");
        osd.setMetas(List.of(meta));
        Relation relation = new Relation(1L,2L,3L,"<meta>sxx</meta>");
        relation.setParent(true);
        osd.setRelations(List.of(relation));
    }

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

    @Test
    public void parseOsdWithMeta() throws IOException, QueryNodeException, ParserException {
        DescendingStringIndexer indexer = new DescendingStringIndexer();
        try (IndexWriter indexWriter = createIndex()) {

            Document doc = new Document();
            String objectAsString = new XmlMapper().writeValueAsString(osd);
            ContentContainer contentContainer = new ContentContainer(objectAsString, new byte[0], "/root/home/sys", "OSD#0");
            org.dom4j.Document xmlDoc = contentContainer.getCombinedDocument();
            log.info("xmlDoc:\n"+xmlDoc.asXML());
            indexer.indexObject(xmlDoc, contentContainer.asNode(), doc, "content", "/objectSystemData/metasets/metaset[typeId='1']/content/html/body", true);
            indexWriter.addDocument(doc);
        }
        IndexSearcher indexSearcher = createIndexSearcher();
        searchWithStandardQuery(indexSearcher, "schnitzel");
        searchWithWildcardQuery(indexSearcher,"*schnitzel*");
    }

    private void searchWithWildcardQuery(IndexSearcher indexSearcher, String queryStringWithWildcard) throws ParserException, IOException {
        InputStream xmlInputStream = new ByteArrayInputStream(wildcardQuery.replace("__queryString__", queryStringWithWildcard).getBytes(StandardCharsets.UTF_8));
        CoreParser  coreParser     = new CoreParser("p", new StandardAnalyzer());
        coreParser.addQueryBuilder("WildcardQuery", new WildcardQueryBuilder());
        Query query = coreParser.parse(xmlInputStream);
        log.info("query: "+query);
        TopDocs topDocs = indexSearcher.search(query, 100);
        assertEquals(1, topDocs.totalHits.value);
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
        coreParser.addQueryBuilder("ExactPointQuery", new ExactPointQueryBuilder());
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
        Map<String, PointsConfig> pointsConfigMap = Map.of("p", longConfig);
        StandardQueryParser queryParser = new StandardQueryParser(new StandardAnalyzer());
        queryParser.setPointsConfigMap(pointsConfigMap);
        Query   query   = queryParser.parse("300", "p");
        TopDocs topDocs = searcher.search(query, 100);
        assertEquals(1, topDocs.totalHits.value);
    }

    private void searchWithStandardQuery(IndexSearcher searcher, String queryString) throws QueryNodeException, IOException {
        StandardQueryParser queryParser = new StandardQueryParser(new StandardAnalyzer());
        Query   query   = queryParser.parse(queryString, "content");
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
