package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.application.CinnamonServer;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.client.CinnamonClientException;
import com.dewarim.cinnamon.model.request.index.ReindexRequest;
import com.dewarim.cinnamon.model.request.search.SearchType;
import com.dewarim.cinnamon.model.response.SearchIdsResponse;
import com.dewarim.cinnamon.model.response.index.IndexInfoResponse;
import com.dewarim.cinnamon.model.response.index.ReindexResponse;
import com.dewarim.cinnamon.test.TestObjectHolder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class IndexAndSearchServletIntegrationTest extends CinnamonIntegrationTest {

    public static Long osdId;
    public static Long folderId;

    @BeforeAll
    public static void initializeObjects() throws IOException, InterruptedException {
        TestObjectHolder toh = new TestObjectHolder(client, "reviewers.acl", userId, createFolderId);
        toh.createOsd("search-me-osd")
                .createFolder("search-me-folder", createFolderId);
        osdId = toh.osd.getId();
        folderId = toh.folder.getId();
        ThreadLocalSqlSession.refreshSession();
        Thread.sleep(CinnamonServer.config.getLuceneConfig().getMillisToWaitBetweenRuns() + 5000L);
    }

    @Test
    public void showInfo() throws IOException {
        IndexInfoResponse indexInfoResponse = client.getIndexInfo(true);
        assertTrue(indexInfoResponse.getDocumentsInIndex() > 0);
        assertTrue(indexInfoResponse.getFoldersInIndex() > 0);
    }

    @Test
    public void searchIds() throws IOException {
        // TODO: filter system indexItems before indexing with XML-Indexers
        SearchIdsResponse response = client.search("<BooleanQuery><Clause occurs='must'><TermQuery fieldName='name'>search-me-osd</TermQuery></Clause></BooleanQuery>", SearchType.OSD);
        SearchIdsResponse adminResponse = adminClient.search("<BooleanQuery><Clause occurs='must'><TermQuery fieldName='name'>search-me-osd</TermQuery></Clause></BooleanQuery>", SearchType.OSD);
        System.out.println(response);
        assertEquals(1, response.getOsdIds().size());
        SearchIdsResponse folderResponse = client.search("<BooleanQuery><Clause occurs='must'><TermQuery fieldName='name'>search-me-folder</TermQuery></Clause></BooleanQuery>", SearchType.FOLDER);
        assertEquals(1, folderResponse.getFolderIds().size());
        SearchIdsResponse allResponse = client.search("<BooleanQuery><Clause occurs='must'><WildcardQuery fieldName='name'>search-me-*</WildcardQuery></Clause></BooleanQuery>", SearchType.ALL);
        assertEquals(1, allResponse.getFolderIds().size());
        assertEquals(1, allResponse.getOsdIds().size());
        assertEquals(osdId, allResponse.getOsdIds().get(0));
        assertEquals(folderId, allResponse.getFolderIds().get(0));
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


}
