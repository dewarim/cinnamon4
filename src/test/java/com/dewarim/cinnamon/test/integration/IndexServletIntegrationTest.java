package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.application.CinnamonServer;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.client.CinnamonClientException;
import com.dewarim.cinnamon.model.request.index.ReindexRequest;
import com.dewarim.cinnamon.model.response.index.IndexInfoResponse;
import com.dewarim.cinnamon.model.response.index.ReindexResponse;
import com.dewarim.cinnamon.test.TestObjectHolder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class IndexServletIntegrationTest extends CinnamonIntegrationTest {

    private static Long osdId;
    private static Long folderId;

    @BeforeAll
    public static void createIndexObjects() throws IOException {
        TestObjectHolder toh = new TestObjectHolder(client, "reviewers.acl", userId, createFolderId);
        toh.createOsd("showInfo - index this")
                .createFolder("showInfo - index this folder", createFolderId);
        osdId = toh.osd.getId();
        folderId = toh.folder.getId();
        ThreadLocalSqlSession.refreshSession();
    }

    @Test
    public void showInfo() throws IOException, InterruptedException {
        Thread.sleep(CinnamonServer.config.getLuceneConfig().getMillisToWaitBetweenRuns() + 5000L);
        IndexInfoResponse indexInfoResponse = client.getIndexInfo(true);
        assertTrue(indexInfoResponse.getDocumentsInIndex() > 0);
        assertTrue(indexInfoResponse.getFoldersInIndex() > 0);

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
    public void reindexAllTheThings() throws IOException {
        ReindexResponse response = adminClient.reindex(new ReindexRequest(List.of(), List.of()));
        assertTrue(response.getDocumentsToIndex() > 0);
        assertTrue(response.getFoldersToIndex() > 0);
    }

    @Test
    public void reindexSpecificObjects()throws IOException{
        ReindexResponse response=adminClient.reindex(new ReindexRequest(List.of(osdId),List.of(folderId)));
        assertEquals(1,response.getFoldersToIndex());
        assertEquals(1,response.getDocumentsToIndex());
    }

}
