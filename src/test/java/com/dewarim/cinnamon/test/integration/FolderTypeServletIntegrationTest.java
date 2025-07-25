package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.api.Constants;
import com.dewarim.cinnamon.client.CinnamonClientException;
import com.dewarim.cinnamon.model.FolderType;
import com.dewarim.cinnamon.model.request.folder.UpdateFolderRequest;
import com.dewarim.cinnamon.test.TestObjectHolder;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.dewarim.cinnamon.ErrorCode.DB_DELETE_FAILED;
import static com.dewarim.cinnamon.ErrorCode.REQUIRES_SUPERUSER_STATUS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FolderTypeServletIntegrationTest extends CinnamonIntegrationTest {

    @Test
    public void create() throws IOException {
        List<FolderType> types = adminClient.createFolderTypes(Collections.singletonList("new-folder-type"));
        assertTrue(types.stream().anyMatch(ft -> ft.getName().equals("new-folder-type")));
    }

    @Test
    public void createWithoutPermission() throws IOException {
        try {
            client.createFolderTypes(Collections.singletonList("new-folder-type"));
        } catch (CinnamonClientException e) {
            assertEquals(REQUIRES_SUPERUSER_STATUS, e.getErrorCode());
        }
    }

    @Test
    public void list() throws IOException {
        List<FolderType> folderTypes = client.listFolderTypes();
        assertTrue(folderTypes.contains(new FolderType(1L, Constants.FOLDER_TYPE_DEFAULT)));
    }

    @Test
    public void delete() throws IOException {
        List<FolderType> types        = adminClient.createFolderTypes(Collections.singletonList("delete-me-folder-type"));
        boolean          deleteResult = adminClient.deleteFolderTypes(types.stream().map(FolderType::getId).collect(Collectors.toList()));
        assertTrue(deleteResult);
    }

    @Test
    public void deleteWithoutPermission() throws IOException {
        List<FolderType> types = adminClient.createFolderTypes(Collections.singletonList("delete-me-forbidden"));
        assertClientError( () ->
                client.deleteFolderTypes(types.stream().map(FolderType::getId).collect(Collectors.toList())),
                REQUIRES_SUPERUSER_STATUS
        );
    }

    @Test
    public void updateFolderTypes() throws IOException {
        List<String>     originalNames = Arrays.asList("one", "two");
        List<FolderType> types         = adminClient.createFolderTypes(originalNames);
        types.get(0).setName("1");
        types.get(1).setName("2");
        List<FolderType> updatedTypes = adminClient.updateFolderTypes(types);
        assertEquals(types.size(), updatedTypes.size());
        assertEquals("1", updatedTypes.get(0).getName());
        assertEquals("2", updatedTypes.get(1).getName());
    }

    @Test
    public void updateWithoutPermission() throws IOException {
        List<FolderType> types = adminClient.createFolderTypes(Collections.singletonList("update-me-not-folder-type"));
        try {
            types.getFirst().setName("forbidden-update");
            client.updateFolderTypes(types);
        } catch (CinnamonClientException e) {
            assertEquals(REQUIRES_SUPERUSER_STATUS, e.getErrorCode());
        }
    }

    @Test
    public void deleteFolderTypeWhichIsInUse() throws IOException {
        var folderType = adminClient.createFolderTypes(List.of("in-use-folder-type")).getFirst();
        var toh = new TestObjectHolder(client, userId)
                .createFolder();
        var folder = toh.folder;
        client.updateFolder(new UpdateFolderRequest(folder.getId(), null, null, null, folderType.getId(), null));
        assertClientError(
                () -> adminClient.deleteFolderTypes(List.of(folderType.getId())),DB_DELETE_FAILED);
    }


}
