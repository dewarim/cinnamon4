package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.application.CinnamonServer;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.Format;
import com.dewarim.cinnamon.model.ObjectSystemData;
import com.dewarim.cinnamon.model.request.index.ReindexRequest;
import com.dewarim.cinnamon.model.request.search.SearchType;
import com.dewarim.cinnamon.model.response.SearchIdsResponse;
import com.dewarim.cinnamon.model.response.index.IndexInfoResponse;
import com.dewarim.cinnamon.model.response.index.ReindexResponse;
import com.dewarim.cinnamon.test.TestObjectHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static com.dewarim.cinnamon.ErrorCode.INVALID_REQUEST;
import static com.dewarim.cinnamon.ErrorCode.REQUIRES_SUPERUSER_STATUS;
import static org.junit.jupiter.api.Assertions.*;

public class IndexAndSearchServletIntegrationTest extends CinnamonIntegrationTest {

    private final static Logger log = LogManager.getLogger(IndexAndSearchServletIntegrationTest.class);

    public static Long osdId;
    public static Long folderId;
    public static Long osdWithContentId;

    @BeforeAll
    public static void initializeObjects() throws IOException, InterruptedException {
        // create indexItem for xml-content
        // the index item is created by CreateTestDB.sql, otherwise it may not be ready/available to
        // the indexerService on time (or: in its thread) for when the OSD is created.

        TestObjectHolder toh          = new TestObjectHolder(adminClient, userId);
        File             bun          = new File("data/cinnamon-bun.png");
        Format           imagePng     = TestObjectHolder.formats.stream().filter(f -> f.getName().equals("image.png")).findFirst().orElseThrow();
        ObjectSystemData relatedOsd   = toh.createOsdWithContent("related image", imagePng, bun).osd;
        Long             relatedOsdId = relatedOsd.getId();
        osdWithContentId = relatedOsdId;

        // with XML content:
        toh.createOsd("search-me-osd")
                .createMetaSetType(true)
                .createRelationType()
                .setSummaryOnOsd("<summary><p>test-summary</p></summary>")
                .createOsdMeta("<xml><copyright>ACME Inc., 2023</copyright></xml>")
                .createRelation(relatedOsdId, "<xml><imageSize x='100' y='200'/></xml>")
                .createFolder("search-me-folder", createFolderId)
                .createFolderMeta("<xml><folder-meta-data archived='no'/></xml>");
        osdId    = toh.osd.getId();
        folderId = toh.folder.getId();
        log.info("created search-me objects: osd: {} folder: {}", osdId, folderId);

        // set xml-content:
        client.lockOsd(osdId);
        client.setContentOnLockedOsd(osdId, 1L, new File("pom.xml"));

        // with JSON content:
        toh.selectFormat("json")
                .createOsdWithContent(new File("src/test/resources/test.json"));

        if(!CinnamonServer.getConfig().getLuceneConfig().isWaitUntilSearchable()) {
            Thread.sleep(CinnamonServer.config.getLuceneConfig().getMillisToWaitBetweenRuns() + 5000L);
        }
        ThreadLocalSqlSession.refreshSession();
    }

    @Test
    public void showInfo() throws IOException {
        IndexInfoResponse indexInfoResponse = client.getIndexInfo(true, true);
        assertTrue(indexInfoResponse.getDocumentsInIndex() > 0);
        assertTrue(indexInfoResponse.getFoldersInIndex() > 0);
        // TODO: would be nice to test with broken XML file.
        // this is just a broken job added via CreateTestDb script.
        assertTrue(indexInfoResponse.getFailedIndexJobs().size() > 0);
        assertEquals(indexInfoResponse.getFailedJobCount(), indexInfoResponse.getFailedIndexJobs().size());
    }

    @Test
    public void searchIds() throws IOException {
        // TODO: filter system indexItems before indexing with XML-Indexers
        // TODO: test filtering for unbrowsable items
        SearchIdsResponse response = client.search("<BooleanQuery><Clause occurs='must'><TermQuery fieldName='name'>search-me-osd</TermQuery></Clause></BooleanQuery>", SearchType.OSD);
        log.info("searchIdsResponse for OSD search: {}", response);
        assertEquals(1, response.getOsdIds().size());
        SearchIdsResponse folderResponse = client.search("<BooleanQuery><Clause occurs='must'><TermQuery fieldName='name'>search-me-folder</TermQuery></Clause></BooleanQuery>", SearchType.FOLDER);
        log.info("searchIdsResponse for FOLDER search: {}", response);
        assertEquals(1, folderResponse.getFolderIds().size());

        SearchIdsResponse allResponse = client.search("<BooleanQuery><Clause occurs='must'><WildcardQuery fieldName='name'>search-me-*</WildcardQuery></Clause></BooleanQuery>", SearchType.ALL);
        assertEquals(1, allResponse.getFolderIds().size());
        assertEquals(1, allResponse.getOsdIds().size());
        assertEquals(osdId, allResponse.getOsdIds().getFirst());
        assertEquals(folderId, allResponse.getFolderIds().getFirst());

    }


    @Test
    public void searchForContent() throws IOException {
        SearchIdsResponse response = client.search("<BooleanQuery><Clause occurs='must'><TermQuery fieldName='xml_content'>cinnamon</TermQuery></Clause></BooleanQuery>", SearchType.OSD);
        log.info("response: {}", mapper.writeValueAsString(response));
        assertTrue(response.getOsdIds().size() > 0);
        long exampleId = response.getOsdIds().getFirst();

        if (CinnamonServer.getConfig().getCinnamonTikaConfig().isUseTika()) {
            SearchIdsResponse imageSearchResponse = client.search("<BooleanQuery><Clause occurs='must'><TermQuery fieldName='tika_meta'>delicious</TermQuery></Clause></BooleanQuery>", SearchType.ALL);
            assertEquals(1, imageSearchResponse.getOsdIds().size());
            assertEquals(osdWithContentId, imageSearchResponse.getOsdIds().getFirst());
        }
        verifyOsdSearchResult(client.search(createTermQuery("is_latest_branch", "true"), SearchType.OSD));
        verifyOsdSearchResult(client.search(createTermQuery("osd_name", "related image"), SearchType.OSD));
        verifyOsdSearchResult(client.search(createPointQuery("osd_id", exampleId), SearchType.OSD));
        LocalDate localDate = LocalDate.now();
        verifyOsdSearchResult(client.search(createWildcardQuery("osd_created",
                localDate.toString().replace("-", "") + "*"), SearchType.OSD));
        // TODO: fix problem with indexing file content and running this search test in the same JVM.
        // ElementNameIndexer verified manually :/
        // verifyOsdSearchResult(client.search(createTermQuery("element_names", "dependency"), SearchType.OSD));

    }

    @Test
    public void searchForJsonContent() throws IOException {
        SearchIdsResponse response = client.search("<BooleanQuery><Clause occurs='must'><TermQuery fieldName='xml_content'>bob</TermQuery></Clause></BooleanQuery>", SearchType.OSD);
        log.info("response: {}", mapper.writeValueAsString(response));
        assertTrue(response.getOsdIds().size() > 0);
        long exampleId = response.getOsdIds().getFirst();

        verifyOsdSearchResult(client.search(createTermQuery("is_latest_branch", "true"), SearchType.OSD));
        verifyOsdSearchResult(client.search(createTermQuery("osd_name", "related image"), SearchType.OSD));
        verifyOsdSearchResult(client.search(createPointQuery("osd_id", exampleId), SearchType.OSD));
        LocalDate localDate = LocalDate.now();
        verifyOsdSearchResult(client.search(createWildcardQuery("osd_created",
                localDate.toString().replace("-", "") + "*"), SearchType.OSD));
        // ElementNameIndexer verified manually :/
        // verifyOsdSearchResult(client.search(createTermQuery("element_names", "dependency"), SearchType.OSD));
        SearchIdsResponse jsonName = client.search("<BooleanQuery><Clause occurs='must'><TermQuery fieldName='json_name'>bob</TermQuery></Clause></BooleanQuery>", SearchType.OSD);
        assertEquals(1, jsonName.getOsdIds().size());
    }

    private void verifyOsdSearchResult(SearchIdsResponse response) {
        assertNotNull(response.getOsdIds());
        assertFalse(response.getOsdIds().isEmpty());
    }

    private String createPointQuery(String fieldName, long query) {
        String pointQuery = "<BooleanQuery><Clause occurs='must'><ExactPointQuery fieldName='" + fieldName + "' value='" + query + "' type='long'/></Clause></BooleanQuery>";
        log.debug("pointQuery: {}", pointQuery);
        return pointQuery;
    }

    private String createTermQuery(String fieldName, String query) {
        String booleanQuery = "<BooleanQuery><Clause occurs='must'><TermQuery fieldName='" + fieldName + "'>" + query + "</TermQuery></Clause></BooleanQuery>";
        log.debug("createTermQuery: {}", booleanQuery);
        return booleanQuery;
    }

    private String createWildcardQuery(String fieldName, String query) {
        String wildQuery = "<BooleanQuery><Clause occurs='must'><WildcardQuery fieldName='" + fieldName + "'>" + query + "</WildcardQuery></Clause></BooleanQuery>";
        log.debug("wildcardQuery: {}", wildQuery);
        return wildQuery;
    }

    @Test
    public void reindexIsForbiddenForNonAdmins() {
        assertClientError(() -> client.reindex(new ReindexRequest()), REQUIRES_SUPERUSER_STATUS);
    }

    @Test
    public void reindexInvalidRequest() {
        assertClientError(() -> adminClient.reindex(new ReindexRequest(List.of(-1L), List.of())), INVALID_REQUEST);
    }

    @Test
    public void reindexAllTheThings() throws IOException, InterruptedException {
        ReindexResponse response = adminClient.reindex(new ReindexRequest(List.of(), List.of()));
        assertTrue(response.getDocumentsToIndex() > 0);
        assertTrue(response.getFoldersToIndex() > 0);
        Thread.sleep(3000);
    }

    @Test
    public void reindexSpecificObjects() throws IOException, InterruptedException {
        ReindexResponse response = adminClient.reindex(new ReindexRequest(List.of(osdId), List.of(folderId)));
        assertEquals(1, response.getFoldersToIndex());
        assertEquals(1, response.getDocumentsToIndex());
        Thread.sleep(3000);
    }

    // ticket 389: meta operations do not trigger reindex
    @Test
    public void updateMetaTest() throws IOException, InterruptedException {
        var toh = new TestObjectHolder(client, userId)
                .createOsd();
        Thread.sleep(1300);
        toh.createOsdMeta("<xml>issue389</xml>");
        Thread.sleep(1300);

        String            termQuery = createTermQuery("meta_content", "issue389");
        SearchIdsResponse response  = client.search(termQuery, SearchType.OSD);
        assertTrue(response.getOsdIds().size() > 0);
        assertEquals(toh.osd.getId(), response.getOsdIds().getFirst());
    }

}
