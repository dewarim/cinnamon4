package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.application.CinnamonServer;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.request.search.SearchType;
import com.dewarim.cinnamon.model.response.index.IndexInfoResponse;
import com.dewarim.cinnamon.test.TestObjectHolder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SearchServletIntegrationTest extends CinnamonIntegrationTest {

    private static Long osdId;
    private static Long folderId;

    @BeforeAll
    public static void createIndexObjects() throws IOException {
        TestObjectHolder toh = new TestObjectHolder(client, "reviewers.acl", userId, createFolderId);
        toh.createOsd("osd-search-me")
                .createFolder("folder-search-me", createFolderId);
        osdId = toh.osd.getId();
        folderId = toh.folder.getId();
        ThreadLocalSqlSession.refreshSession();
    }

    @Test
    public void searchOsd() throws IOException, InterruptedException {
        Thread.sleep(CinnamonServer.config.getLuceneConfig().getMillisToWaitBetweenRuns() + 5000L);
        IndexInfoResponse indexInfoResponse = client.getIndexInfo(true);
        assertTrue(indexInfoResponse.getDocumentsInIndex() > 0);
        assertTrue(indexInfoResponse.getFoldersInIndex() > 0);

        // TODO: add query
        // TODO: index systemmeta-fields
        // TODO: filter system indexItems before indexing with XML-Indexers
        client.search("", SearchType.OSD);

    }


}
