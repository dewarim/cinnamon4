package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.application.CinnamonServer;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.client.CinnamonClientException;
import com.dewarim.cinnamon.dao.FolderDao;
import com.dewarim.cinnamon.model.Format;
import com.dewarim.cinnamon.model.IndexItem;
import com.dewarim.cinnamon.model.index.IndexType;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class IndexAndSearchServletIntegrationTest extends CinnamonIntegrationTest {

    private final static Logger log = LogManager.getLogger(IndexAndSearchServletIntegrationTest.class);

    public static Long osdId;
    public static Long folderId;

    @BeforeAll
    public static void initializeObjects() throws IOException, InterruptedException {
        TestObjectHolder toh = new TestObjectHolder(client, userId);
        toh.createOsd("search-me-osd")
                .createFolder("search-me-folder", createFolderId);
        osdId = toh.osd.getId();
        folderId = toh.folder.getId();
        log.info("created search-me objects: osd: " + osdId + " folder: " + folderId);

        // create indexItem for xml-content
        Format xml = adminClient.createFormat("text/xml", "xml", "xml-content", 1L);
        adminClient.createIndexItem(new IndexItem("xml_content",true,false,false,
                true,"Xml Content Index", "/objectSystemData/content/descendant::*", "boolean(/objectSystemData/formatId[text()='"+xml.getId()+"'])", false, IndexType.DEFAULT_INDEXER ));
        // set xml-content:
        client.lockOsd(osdId);
        client.setContentOnLockedOsd(osdId, xml.getId(), new File("pom.xml"));

        Thread.sleep(CinnamonServer.config.getLuceneConfig().getMillisToWaitBetweenRuns() + 5000L);
        ThreadLocalSqlSession.refreshSession();
    }

//    @Test
//    public void searchConditionTest(){
//        String searchCondition = "boolean(/osd/id[text()='1'])";
//        Document document = ParamParser.parseXmlToDocument("<osd><id>1</id></osd>");
//        assertEquals("true", document.valueOf(searchCondition));
//    }

    @Test
    public void showInfo() throws IOException {
        IndexInfoResponse indexInfoResponse = client.getIndexInfo(true);
        assertTrue(indexInfoResponse.getDocumentsInIndex() > 0);
        assertTrue(indexInfoResponse.getFoldersInIndex() > 0);
    }

    @Test
    public void searchIds() throws IOException {
        // TODO: filter system indexItems before indexing with XML-Indexers
        // TODO: test filtering for unbrowsable items
        SearchIdsResponse response = client.search("<BooleanQuery><Clause occurs='must'><TermQuery fieldName='name'>search-me-osd</TermQuery></Clause></BooleanQuery>", SearchType.OSD);
        log.info("searchIdsResponse for OSD search: " + response);
        assertEquals(1, response.getOsdIds().size());
        SearchIdsResponse folderResponse = client.search("<BooleanQuery><Clause occurs='must'><TermQuery fieldName='name'>search-me-folder</TermQuery></Clause></BooleanQuery>", SearchType.FOLDER);
        log.info("searchIdsResponse for FOLDER search: " + response);
        assertEquals(1, folderResponse.getFolderIds().size());

        SearchIdsResponse allResponse = client.search("<BooleanQuery><Clause occurs='must'><WildcardQuery fieldName='name'>search-me-*</WildcardQuery></Clause></BooleanQuery>", SearchType.ALL);
        assertEquals(1, allResponse.getFolderIds().size());
        assertEquals(1, allResponse.getOsdIds().size());
        assertEquals(osdId, allResponse.getOsdIds().get(0));
        assertEquals(folderId, allResponse.getFolderIds().get(0));
    }

    @Test
    public void searchForContent() throws IOException{
        SearchIdsResponse response = client.search("<BooleanQuery><Clause occurs='must'><TermQuery fieldName='xml_content'>maven</TermQuery></Clause></BooleanQuery>", SearchType.OSD);
        assertEquals(1, response.getOsdIds().size());
        assertEquals(osdId, response.getOsdIds().get(0));
    }

    @Test
    public void reindexIsForbiddenForNonAdmins() {
        CinnamonClientException ex = assertThrows(CinnamonClientException.class, () -> client.reindex(new ReindexRequest()));
        assertEquals(ErrorCode.REQUIRES_SUPERUSER_STATUS, ex.getErrorCode());
    }

    @Test
    public void reindexInvalidRequest() {
        CinnamonClientException ex = assertThrows(CinnamonClientException.class, () -> adminClient.reindex(new ReindexRequest(List.of(-1L), List.of())));
        assertEquals(ErrorCode.INVALID_REQUEST, ex.getErrorCode());
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

    // TODO: maybe move to a FolderDaoIntegrationTest
    // but then, folderPath is currently only used in IndexService.
    @Test
    public void verifyFolderPathOrdering() throws IOException{
        TestObjectHolder toh = new TestObjectHolder(adminClient, adminId);
        toh.createFolder("f1", 1L)
        .createFolder("f2", toh.folder.getId())
        .createFolder("f3", toh.folder.getId())
        .createFolder("f4", toh.folder.getId())
        .createFolder("f5", toh.folder.getId());
        String withAncestors = new FolderDao().setSqlSession(null).getFolderPath(toh.folder.getId());
        assertEquals("/root/f1/f2/f3/f4/f5", withAncestors);
    }
}
