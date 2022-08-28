package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.client.CinnamonClientException;
import com.dewarim.cinnamon.model.MetasetType;
import com.dewarim.cinnamon.model.request.CreateMetaRequest;
import com.dewarim.cinnamon.test.TestObjectHolder;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class MetasetTypeServletIntegrationTest extends CinnamonIntegrationTest {

    @Test
    public void listMetasetTypes() throws IOException {
        List<MetasetType> metasetTypes = client.listMetasetTypes();

        assertNotNull(metasetTypes);
        assertFalse(metasetTypes.isEmpty());

        Optional<MetasetType> typeOpt = metasetTypes.stream().filter(metasetType -> metasetType.getName().equals("license"))
                .findFirst();
        assertTrue(typeOpt.isPresent());
        MetasetType type = typeOpt.get();
        assertThat(type.getId(), equalTo(2L));
    }

    @Test
    public void createMetasetTypeHappyPath() throws IOException {
        var metasetType = adminClient.createMetasetType("happy", true);
        assertEquals("happy", metasetType.getName());
        assertTrue(metasetType.getUnique());
    }

    @Test
    public void createDuplicateMetasetType() throws IOException {
        var metasetType = adminClient.createMetasetType("duplicate", true);
        var ex = assertThrows(CinnamonClientException.class,
                () -> adminClient.createMetasetType("duplicate", true));
        assertEquals(ErrorCode.DB_INSERT_FAILED, ex.getErrorCode());
    }

    @Test
    public void createMetasetTypeNonSuperuser() {
        var ex = assertThrows(CinnamonClientException.class,
                () -> client.createMetasetType("non-super", false));
        assertEquals(ErrorCode.REQUIRES_SUPERUSER_STATUS, ex.getErrorCode());
    }

    @Test
    public void updateMetasetTypeHappyPath() throws IOException {
        var metasetType = adminClient.createMetasetType("for-update", true);
        metasetType.setName("new-update");
        adminClient.updateMetasetType(metasetType);
        var types = client.listMetasetTypes();
        assertEquals(1, types.stream().filter(type -> type.getName().equals("new-update")).count());
    }

    @Test
    public void updateMetasetTypeNonSuperuser() throws IOException {
        var metasetType = adminClient.createMetasetType("for-update2", false);
        metasetType.setName("expect-fail");
        var ex = assertThrows(CinnamonClientException.class, () ->
                client.updateMetasetType(metasetType));
        assertEquals(ErrorCode.REQUIRES_SUPERUSER_STATUS, ex.getErrorCode());
    }

    @Test
    public void deleteMetasetTypeHappyPath() throws IOException {
        var metasetType = adminClient.createMetasetType("for-delete", true);
        assertTrue(adminClient.deleteMetasetType(metasetType.getId()));
        long remaining = client.listMetasetTypes().stream().filter(type -> type.getName().equals("for-delete")).count();
        assertEquals(0, remaining);
    }

    @Test
    public void deleteMetasetTypeWhichIsInUse() throws IOException {
        var toh         = new TestObjectHolder(adminClient, "reviewers.acl", userId, createFolderId);
        var osd         = toh.createOsd("delete-metasetType-in-use-test").osd;
        var metasetType = adminClient.createMetasetType("test-in-use", false);
        client.lockOsd(osd.getId());
        client.createOsdMeta(new CreateMetaRequest(osd.getId(), "<xml>test</xml>", metasetType.getId()));
        var ex = assertThrows(CinnamonClientException.class, () -> adminClient.deleteMetasetType(metasetType.getId()));
        assertEquals(ErrorCode.DB_DELETE_FAILED, ex.getErrorCode());
    }

    @Test
    public void deleteMetasetTypeNonSuperuser() throws IOException {
        var metasetType = adminClient.createMetasetType("non-super-delete", true);
        var ex = assertThrows(CinnamonClientException.class, () ->
                client.deleteMetasetType(metasetType.getId()));
        assertEquals(ErrorCode.REQUIRES_SUPERUSER_STATUS, ex.getErrorCode());
    }
}
