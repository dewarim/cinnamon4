package com.dewarim.cinnamon.test;

import com.dewarim.cinnamon.application.CinnamonServer;
import com.dewarim.cinnamon.client.CinnamonClient;
import com.dewarim.cinnamon.model.Acl;
import com.dewarim.cinnamon.model.Folder;
import com.dewarim.cinnamon.model.Format;
import com.dewarim.cinnamon.model.ObjectSystemData;
import com.dewarim.cinnamon.model.request.index.ReindexRequest;
import com.dewarim.cinnamon.model.request.search.SearchType;
import com.dewarim.cinnamon.model.response.SearchIdsResponse;
import com.dewarim.cinnamon.model.response.index.IndexInfoResponse;
import com.dewarim.cinnamon.model.response.index.ReindexResponse;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Disabled("only run manually against a server @ port 19999 " +
        "- otherwise we get concurrency issues with background indexing and database commits in threads")
public class ManualTest {

    private final static Logger log = LogManager.getLogger(ManualTest.class);

    private static       boolean        initialized = false;
    private static       CinnamonClient client;
    private static       Acl            defaultCreationAcl;
    private static       Folder         creationFolder;
    private static       long           createFolderId;
    private static       long           adminId     = 1L;
    private static       long           userId      = 1L;
    private static       long           osdId;
    private static       long           folderId;
    private static final XmlMapper      mapper      = new XmlMapper();
    private static       long           osdWithContentId;

    @BeforeAll
    public static void setup() throws IOException, InterruptedException {
        if (initialized) {
            return;
        }
        client = new CinnamonClient(8080, "localhost", "http", "admin", "admin");
        var toh = new TestObjectHolder(client, adminId);
        defaultCreationAcl                       = TestObjectHolder.acls.get(0);
        creationFolder                           = client.getFoldersByPath("/creation", false).get(0);
        createFolderId                           = creationFolder.getId();
        TestObjectHolder.defaultCreationFolderId = createFolderId;
        TestObjectHolder.defaultCreationAcl      = defaultCreationAcl;


        File   bun      = new File("data/cinnamon-bun.png");
        Format imagePng = TestObjectHolder.formats.stream().filter(f -> f.getName().equals("image.png")).findFirst().orElseThrow();
        toh.folder = creationFolder;
        toh.acl    = defaultCreationAcl;
        ObjectSystemData relatedOsd   = toh.createOsdWithContent("related image", imagePng, bun).osd;
        Long             relatedOsdId = relatedOsd.getId();
        osdWithContentId = relatedOsdId;
        toh.createFolder()
                .createOsd("search-me-osd")
                .createMetaSetType(true)
                .createRelationType()
                .createOsdMeta("<xml><copyright>ACME Inc., 2023</copyright></xml>")
                .createRelation(relatedOsdId, "<xml><imageSize x='100' y='200'/></xml>")
                .createFolder("search-me-folder", toh.folder.getId())
                .createFolderMeta("<xml><folder-meta-data archived='no'/></xml>");
        osdId    = toh.osd.getId();
        folderId = toh.folder.getId();
        log.info("created search-me objects: osd: {} folder: {}", osdId, folderId);

        // set xml-content:
        client.lockOsd(osdId);
        client.setContentOnLockedOsd(osdId, 1L, new File("pom.xml"));

        Thread.sleep(CinnamonServer.config.getLuceneConfig().getMillisToWaitBetweenRuns() + 5000L);
        initialized = true;
    }

    @Test
    public void manualTest() throws IOException, InterruptedException {
        setup();
        SearchIdsResponse response = client.search("<BooleanQuery><Clause occurs='must'><TermQuery fieldName='name'>search-me-osd</TermQuery></Clause></BooleanQuery>", SearchType.OSD);
        log.info("searchIdsResponse for OSD search: {}", response);
        // may have more than 1 result depending on how often you run this test.
        assertTrue(response.getOsdIds().size() > 0);
    }

    @Test
    public void showInfo() throws IOException, InterruptedException {
        setup();
        IndexInfoResponse indexInfoResponse = client.getIndexInfo(true, true);
        assertTrue(indexInfoResponse.getDocumentsInIndex() > 0);
        assertTrue(indexInfoResponse.getFoldersInIndex() > 0);
        // TODO: would be nice to test with broken XML file.
        // this is just a broken job added via CreateTestDb script.
        assertTrue(indexInfoResponse.getFailedIndexJobs().size() > 0);
        assertEquals(indexInfoResponse.getFailedJobCount(), indexInfoResponse.getFailedIndexJobs().size());
    }


    @Test
    public void searchIds() throws IOException, InterruptedException {
        setup();
        // TODO: filter system indexItems before indexing with XML-Indexers
        // TODO: test filtering for unbrowsable items
        SearchIdsResponse response = client.search("<BooleanQuery><Clause occurs='must'><TermQuery fieldName='name'>search-me-osd</TermQuery></Clause></BooleanQuery>", SearchType.OSD);
        log.info("searchIdsResponse for OSD search: {}", response);
        assertTrue(response.getOsdIds().size() > 0);
        SearchIdsResponse folderResponse = client.search("<BooleanQuery><Clause occurs='must'><TermQuery fieldName='name'>search-me-folder</TermQuery></Clause></BooleanQuery>", SearchType.FOLDER);
        log.info("searchIdsResponse for FOLDER search: {}", response);
        assertTrue(folderResponse.getFolderIds().size() > 0);

        SearchIdsResponse allResponse = client.search("<BooleanQuery><Clause occurs='must'><WildcardQuery fieldName='name'>search-me-*</WildcardQuery></Clause></BooleanQuery>", SearchType.ALL);
        assertTrue(allResponse.getFolderIds().size() > 0);
        assertTrue(allResponse.getOsdIds().size() > 0);
        assertTrue(allResponse.getOsdIds().contains(osdId));
        assertTrue(allResponse.getFolderIds().contains(folderId));
    }

    @Test
    public void searchForContent() throws IOException, InterruptedException {
        setup();
        SearchIdsResponse response = client.search("<BooleanQuery><Clause occurs='must'><TermQuery fieldName='xml_content'>cinnamon</TermQuery></Clause></BooleanQuery>", SearchType.OSD);
        log.info("response: {}", mapper.writeValueAsString(response));
        assertTrue(response.getOsdIds().size() > 0);
        long exampleId = response.getOsdIds().get(0);

        if (CinnamonServer.getConfig().getCinnamonTikaConfig().isUseTika()) {
            SearchIdsResponse imageSearchResponse = client.search("<BooleanQuery><Clause occurs='must'><TermQuery fieldName='xml_content'>delicious</TermQuery></Clause></BooleanQuery>", SearchType.ALL);
            assertEquals(1, imageSearchResponse.getOsdIds().size());
            assertEquals(osdWithContentId, imageSearchResponse.getOsdIds().get(0));
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
    public void reindexAllTheThings() throws IOException, InterruptedException {
        setup();
        ReindexResponse response = client.reindex(new ReindexRequest(List.of(), List.of()));
        assertTrue(response.getDocumentsToIndex() > 0);
        assertTrue(response.getFoldersToIndex() > 0);
        Thread.sleep(3000);
    }

    @Test
    public void reindexSpecificObjects() throws IOException, InterruptedException {
        setup();
        ReindexResponse response = client.reindex(new ReindexRequest(List.of(osdId), List.of(folderId)));
        assertEquals(1, response.getFoldersToIndex());
        assertEquals(1, response.getDocumentsToIndex());
        Thread.sleep(3000);
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

    private String createWildcardQuery(String fieldName, String query) {
        String wildQuery = "<BooleanQuery><Clause occurs='must'><WildcardQuery fieldName='" + fieldName + "'>" + query + "</WildcardQuery></Clause></BooleanQuery>";
        log.debug("wildcardQuery: {}", wildQuery);
        return wildQuery;
    }

    private String createTermQuery(String fieldName, String query) {
        String booleanQuery = "<BooleanQuery><Clause occurs='must'><TermQuery fieldName='" + fieldName + "'>" + query + "</TermQuery></Clause></BooleanQuery>";
        log.debug("createTermQuery: {}", booleanQuery);
        return booleanQuery;
    }

    // ticket 389: meta operations do not trigger reindex
    @Test
    public void updateMetaTest() throws IOException, InterruptedException {
        var toh = new TestObjectHolder(client, userId)
                .createOsd();
        Thread.sleep(4000);

        toh.createOsdMeta("<xml>issue389</xml>");
        Thread.sleep(4000);

        String termQuery = createTermQuery("meta_content", "issue389");
        SearchIdsResponse response = client.search(termQuery, SearchType.OSD);
        assertTrue(response.getOsdIds().size() > 0);
        assertTrue(response.getOsdIds().contains(toh.osd.getId()));
    }


}
