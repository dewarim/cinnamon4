package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.dao.FolderDao;
import com.dewarim.cinnamon.test.TestObjectHolder;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FolderDaoIntegrationTest extends CinnamonIntegrationTest {

    @Test
    public void verifyFolderPathOrdering() throws IOException, InterruptedException {
        TestObjectHolder toh = new TestObjectHolder(adminClient, adminId);
        toh.createFolder("f1", 1L)
                .createFolder("f2", toh.folder.getId())
                .createFolder("f3", toh.folder.getId())
                .createFolder("f4", toh.folder.getId())
                .createFolder("f5", toh.folder.getId());
        // getFolderPath is used by IndexService
        Thread.sleep(300);
        ThreadLocalSqlSession.refreshSession();
        String withAncestors = new FolderDao().getFolderPath(toh.folder.getId());
        assertEquals("/root/f1/f2/f3/f4/f5", withAncestors);
    }
}
