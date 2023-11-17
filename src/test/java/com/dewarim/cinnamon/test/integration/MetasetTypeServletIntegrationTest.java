package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.model.MetasetType;
import com.dewarim.cinnamon.model.request.meta.CreateMetaRequest;
import com.dewarim.cinnamon.test.TestObjectHolder;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static com.dewarim.cinnamon.ErrorCode.*;
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
        adminClient.createMetasetType("duplicate", true);
        assertClientError(() -> adminClient.createMetasetType("duplicate", true), DB_INSERT_FAILED);
    }

    @Test
    public void createMetasetTypeNonSuperuser() {
        assertClientError(
                () -> client.createMetasetType("non-super", false), REQUIRES_SUPERUSER_STATUS);
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
        assertClientError(() ->
                client.updateMetasetType(metasetType), REQUIRES_SUPERUSER_STATUS);
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
        var toh = new TestObjectHolder(client, userId);
        var osd = toh.createOsd("delete-metasetType-in-use-test").osd;
        var metasetType = adminClient.createMetasetType("test-in-use", false);
        client.lockOsd(osd.getId());
        client.createOsdMeta(new CreateMetaRequest(osd.getId(), "<xml>test</xml>", metasetType.getId()));
        assertClientError(() -> adminClient.deleteMetasetType(metasetType.getId()), DB_DELETE_FAILED);
    }

    @Test
    public void deleteMetasetTypeNonSuperuser() throws IOException {
        var metasetType = adminClient.createMetasetType("non-super-delete", true);
        assertClientError(() ->
                client.deleteMetasetType(metasetType.getId()), REQUIRES_SUPERUSER_STATUS);
    }
}
