package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.application.CinnamonServer;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.response.index.IndexInfoResponse;
import com.dewarim.cinnamon.test.TestObjectHolder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class IndexServletIntegrationTest extends CinnamonIntegrationTest {

//    @Test
    public void showInfo() throws IOException, InterruptedException {
        new TestObjectHolder(client, "reviewers.acl", userId, createFolderId).createOsd("showInfo - index this");
        ThreadLocalSqlSession.refreshSession();
        Thread.sleep(CinnamonServer.config.getLuceneConfig().getMillisToWaitBetweenRuns() + 5000L);
        IndexInfoResponse indexInfoResponse = client.getIndexInfo(true);
        assertTrue(indexInfoResponse.getDocumentsInIndex() > 0);

    }

}
