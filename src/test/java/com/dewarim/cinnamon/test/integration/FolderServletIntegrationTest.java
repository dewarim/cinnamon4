package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.DefaultPermission;
import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.client.CinnamonClientException;
import com.dewarim.cinnamon.model.Acl;
import com.dewarim.cinnamon.model.Folder;
import com.dewarim.cinnamon.model.Meta;
import com.dewarim.cinnamon.model.MetasetType;
import com.dewarim.cinnamon.model.ObjectSystemData;
import com.dewarim.cinnamon.model.relations.Relation;
import com.dewarim.cinnamon.model.relations.RelationType;
import com.dewarim.cinnamon.model.request.CreateMetaRequest;
import com.dewarim.cinnamon.model.request.DeleteMetaRequest;
import com.dewarim.cinnamon.model.request.IdListRequest;
import com.dewarim.cinnamon.model.request.MetaRequest;
import com.dewarim.cinnamon.model.request.SetSummaryRequest;
import com.dewarim.cinnamon.model.request.folder.CreateFolderRequest;
import com.dewarim.cinnamon.model.request.folder.FolderPathRequest;
import com.dewarim.cinnamon.model.request.folder.FolderRequest;
import com.dewarim.cinnamon.model.request.folder.SingleFolderRequest;
import com.dewarim.cinnamon.model.request.folder.UpdateFolderRequest;
import com.dewarim.cinnamon.model.response.FolderWrapper;
import com.dewarim.cinnamon.model.response.Summary;
import com.dewarim.cinnamon.model.response.SummaryWrapper;
import com.dewarim.cinnamon.test.TestObjectHolder;
import org.apache.http.HttpResponse;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.dewarim.cinnamon.DefaultPermission.*;
import static com.dewarim.cinnamon.ErrorCode.*;
import static com.dewarim.cinnamon.api.Constants.ROOT_FOLDER_NAME;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class FolderServletIntegrationTest extends CinnamonIntegrationTest {

    @Test
    public void setSummaryHappyPath() throws IOException {
        var folderId = new TestObjectHolder(client,userId).createFolder().folder.getId();
        SetSummaryRequest summaryRequest = new SetSummaryRequest(folderId, "a summary");
        HttpResponse      response       = sendStandardRequest(UrlMapping.FOLDER__SET_SUMMARY, summaryRequest);
        assertResponseOkay(response);

        IdListRequest idListRequest  = new IdListRequest(Collections.singletonList(folderId));
        HttpResponse  verifyResponse = sendStandardRequest(UrlMapping.FOLDER__GET_SUMMARIES, idListRequest);
        assertResponseOkay(verifyResponse);
        SummaryWrapper wrapper = mapper.readValue(verifyResponse.getEntity().getContent(), SummaryWrapper.class);
        assertThat(wrapper.getSummaries().get(0).getContent(), equalTo("a summary"));
    }

    @Test
    public void setSummaryMissingPermission() throws IOException {
        var folderId = prepareAclGroupWithPermissions(List.of()).createFolder().folder.getId();
        assertClientError(() -> client.setFolderSummary(folderId, "a summary"), NO_WRITE_SYS_METADATA_PERMISSION);
    }

    @Test
    public void setSummaryMissingObject() throws IOException {
        SetSummaryRequest summaryRequest = new SetSummaryRequest(Long.MAX_VALUE, "a summary");
        HttpResponse      response       = sendStandardRequest(UrlMapping.FOLDER__SET_SUMMARY, summaryRequest);
        assertCinnamonError(response, ErrorCode.OBJECT_NOT_FOUND);
    }

    @Test
    public void getSummaryHappyPath() throws IOException {
        var           toh       = new TestObjectHolder(client, userId).createFolder().setSummaryOnFolder("foo-folder");
        List<Summary> summaries = client.getFolderSummaries(List.of(toh.folder.getId()));
        assertNotNull(summaries);
        assertFalse(summaries.isEmpty());
        assertThat(summaries.get(0).getContent(), equalTo("foo-folder"));
    }

    @Test
    public void getSummariesMissingPermission() throws IOException {
        var folderId = prepareAclGroupWithPermissions(List.of(DefaultPermission.WRITE_OBJECT_SYS_METADATA))
                .createFolder().setSummaryOnFolder("foo").folder.getId();
        assertClientError(() -> client.getFolderSummaries(List.of(folderId)), NO_READ_OBJECT_SYS_METADATA_PERMISSION);
    }

    @Test
    public void getFolderHappyPath() throws IOException {
        var toh = new TestObjectHolder(client, userId)
                .createFolder();
        List<Folder> folders = client.getFolderByIdWithAncestors(toh.folder.getId(), false);
        assertEquals(3, folders.size());
        assertTrue(folders.stream().anyMatch(folder -> folder.getId().equals(toh.folder.getId())));
        assertTrue(folders.stream().anyMatch(folder -> folder.getId().equals(createFolderId)));
        assertTrue(folders.stream().anyMatch(folder -> folder.getName().equals(ROOT_FOLDER_NAME)));
    }

    @Test
    public void rootFolderShouldHaveOneSubfolder() throws IOException {
        Folder folder = client.getFolderById(1L, false);
        assertTrue(folder.isHasSubfolders());
    }

    @Test
    public void newFolderShouldNotHaveSubfolder() throws IOException {
        Folder newFolder = client.createFolder(createFolderId, "new-folder-without-subfolder", userId, 1L, 1L);
        assertFalse(newFolder.isHasSubfolders());
    }

    @Test
    public void getFolderInvalidRequest() throws IOException {
        SingleFolderRequest singleRequest = new SingleFolderRequest(0L, false);
        HttpResponse        response      = sendStandardRequest(UrlMapping.FOLDER__GET_FOLDER, singleRequest);
        assertCinnamonError(response, ErrorCode.INVALID_REQUEST);
    }

    @Test
    public void getUnknownFolder() throws IOException {
        SingleFolderRequest singleRequest = new SingleFolderRequest(Long.MAX_VALUE, false);
        HttpResponse        response      = sendStandardRequest(UrlMapping.FOLDER__GET_FOLDER, singleRequest);
        assertCinnamonError(response, FOLDER_NOT_FOUND);
    }

    @Test
    public void getFoldersHappyPath() throws IOException {
        var          folder  = new TestObjectHolder(client, userId).createFolder().folder;
        List<Folder> folders = client.getFolders(List.of(folder.getId()), false);
        assertEquals(1, folders.size());
        assertEquals(folder, folders.get(0));
    }

    @Test
    public void getFoldersInvalidRequest() throws IOException {
        FolderRequest folderRequest = new FolderRequest(Collections.singletonList(0L), false);
        HttpResponse  response      = sendStandardRequest(UrlMapping.FOLDER__GET_FOLDERS, folderRequest);
        assertCinnamonError(response, ErrorCode.INVALID_REQUEST);
    }


    @Test
    public void getUnknownFolders() throws IOException {
        FolderRequest folderRequest = new FolderRequest(Collections.singletonList(Long.MAX_VALUE), false);
        HttpResponse  response      = sendStandardRequest(UrlMapping.FOLDER__GET_FOLDERS, folderRequest);
        assertCinnamonError(response, FOLDER_NOT_FOUND);
    }

    @Test
    public void getFolderByPathInvalidRequest() throws IOException {
        FolderPathRequest request  = new FolderPathRequest("", true);
        HttpResponse      response = sendStandardRequest(UrlMapping.FOLDER__GET_FOLDER_BY_PATH, request);
        assertCinnamonError(response, ErrorCode.INVALID_REQUEST);
    }

    @Test
    public void getFolderByPathBadParameter() throws IOException {
        // "//" is not allowed
        FolderPathRequest request  = new FolderPathRequest("http://", true);
        HttpResponse      response = sendStandardRequest(UrlMapping.FOLDER__GET_FOLDER_BY_PATH, request);
        assertCinnamonError(response, ErrorCode.INVALID_FOLDER_PATH_STRUCTURE);

        // trailing "/" is not allowed
        FolderPathRequest trailingRequest  = new FolderPathRequest("http://", true);
        HttpResponse      trailingResponse = sendStandardRequest(UrlMapping.FOLDER__GET_FOLDER_BY_PATH, trailingRequest);
        assertCinnamonError(trailingResponse, ErrorCode.INVALID_FOLDER_PATH_STRUCTURE);
    }

    @Test
    public void getFolderByPathFolderNotFound() throws IOException {
        FolderPathRequest request  = new FolderPathRequest("/foo/bar", true);
        HttpResponse      response = sendStandardRequest(UrlMapping.FOLDER__GET_FOLDER_BY_PATH, request);
        assertCinnamonError(response, ErrorCode.OBJECT_NOT_FOUND);
    }

    @Test
    public void getFolderByPathHappyPath() throws IOException {
        String       folderName  = new TestObjectHolder(client, userId).createFolder().folder.getName();
        List<Folder> folders     = client.getFoldersByPath("/creation/" + folderName);
        List<String> folderNames = folders.stream().map(Folder::getName).toList();
        assertTrue(folderNames.contains("root"));
        assertTrue(folderNames.contains("creation"));
        assertTrue(folderNames.contains(folderName));
    }

    /*
     * Note: other Meta requests are tested in OsdServletIntegrationTest.
     */
    @Test
    public void getMetaInvalidRequest() throws IOException {
        MetaRequest  request      = new MetaRequest();
        HttpResponse metaResponse = sendStandardRequest(UrlMapping.FOLDER__GET_META, request);
        assertCinnamonError(metaResponse, ErrorCode.INVALID_REQUEST);
    }

    @Test
    public void getMetaObjectNotFound() throws IOException {
        MetaRequest  request      = new MetaRequest(Long.MAX_VALUE, null);
        HttpResponse metaResponse = sendStandardRequest(UrlMapping.FOLDER__GET_META, request);
        assertCinnamonError(metaResponse, ErrorCode.OBJECT_NOT_FOUND);
    }

    @Test
    public void getMetaWithoutReadPermission() throws IOException {
        var folderId = prepareAclGroupWithPermissions(List.of(BROWSE)).createFolder().folder.getId();
        assertClientError(() -> client.getFolderMetas(folderId), ErrorCode.NO_READ_CUSTOM_METADATA_PERMISSION);
    }

    @Test
    public void getMetaHappyPath() throws IOException {
        var folder = prepareAclGroupWithPermissions(List.of(READ_OBJECT_CUSTOM_METADATA))
                .createFolder().createFolderMeta("my meta").folder;
        Meta folderMeta = client.getFolderMetas(folder.getId()).get(0);
        assertEquals("my meta", folderMeta.getContent());
    }

    @Test
    public void createMetaInvalidRequest() throws IOException {
        CreateMetaRequest request = new CreateMetaRequest();
        try {
            client.createFolderMeta(request);
        } catch (CinnamonClientException e) {
            assertEquals(e.getErrorCode(), ErrorCode.INVALID_REQUEST);
        }
    }

    @Test
    public void createMetaObjectNotFound() throws IOException {
        CreateMetaRequest request = new CreateMetaRequest(Long.MAX_VALUE, "foo", 1L);
        try {
            client.createFolderMeta(request);
        } catch (CinnamonClientException e) {
            assertEquals(e.getErrorCode(), OBJECT_NOT_FOUND);
        }
    }

    @Test
    public void createMetaObjectNotWritable() throws IOException {
        var folderId = prepareAclGroupWithPermissions(List.of())
                .createFolder().folder.getId();
        CreateMetaRequest request = new CreateMetaRequest(folderId, "foo", 1L);
        assertClientError(() -> client.createFolderMeta(request), NO_WRITE_CUSTOM_METADATA_PERMISSION);
    }

    @Test
    public void createMetaMetasetTypeByIdNotFound() throws IOException {
        var               folderId = new TestObjectHolder(client, userId).createFolder().folder.getId();
        CreateMetaRequest request  = new CreateMetaRequest(folderId, "foo", Long.MAX_VALUE);
        assertClientError(() -> client.createFolderMeta(request), METASET_TYPE_NOT_FOUND);
    }

    @Test
    public void createMetaMetasetIsUniqueAndExists() throws IOException {
        Acl acl = getReviewerAcl();
        Folder folder = client.createFolder(createFolderId, "createMetaMetasetIsUniqueAndExists",
                userId, acl.getId(), 1L);
        MetasetType type = adminClient.createMetasetType("unique metaset type", true);

        CreateMetaRequest request = new CreateMetaRequest(folder.getId(), "duplicate license", type.getId());
        client.createFolderMeta(request);
        assertClientError(() -> client.createFolderMeta(request), METASET_IS_UNIQUE_AND_ALREADY_EXISTS);
    }

    @Test
    public void createFolderMetasetHappyWithExistingMeta() throws IOException {
        var folderId = new TestObjectHolder(client, userId)
                .createFolder().folder.getId();
        client.createFolderMeta(folderId, "comment 1", 1L);
        client.createFolderMeta(folderId, "comment 2", 1L);
        List<Meta> folderMetas = client.getFolderMetas(folderId);
        assertTrue(folderMetas.stream().map(Meta::getContent).toList().containsAll(List.of("comment 1", "comment 2")));
    }

    @Test
    public void createOsdMetasetHappyPath() throws IOException {
        long osdId = new TestObjectHolder(client, userId)
                .createOsd("createOsdMetasetHappyPath").osd.getId();
        Meta licenseMeta = client.createOsdMeta(osdId, "new license meta", 2L);
        assertEquals("new license meta", licenseMeta.getContent());
        assertEquals(2, licenseMeta.getTypeId());
    }

    @Test
    public void deleteAllMetas() throws IOException {
        Folder folder = client.createFolder(createFolderId, "folder-deleteAllMetas", userId, getReviewerAcl().getId(), 1L);
        client.createFolderMeta(new CreateMetaRequest(folder.getId(), "...", 1L));
        client.createFolderMeta(new CreateMetaRequest(folder.getId(), "...", 1L));
        client.deleteAllFolderMeta(folder.getId());
        assertEquals(0, client.getFolderMetas(folder.getId()).size());
    }

    @Test
    public void deleteMetaInvalidRequest() throws IOException {
        DeleteMetaRequest deleteRequest = new DeleteMetaRequest();
        HttpResponse      metaResponse  = sendStandardRequest(UrlMapping.FOLDER__DELETE_META, deleteRequest);
        assertCinnamonError(metaResponse, ErrorCode.INVALID_REQUEST);
    }

    @Test
    public void deleteMetaWithoutPermission() throws IOException {
        var meta = prepareAclGroupWithPermissions(List.of())
                .createFolder().createFolderMeta("some meta").meta;
        assertClientError(() -> client.deleteFolderMeta(meta.getId()), NO_WRITE_CUSTOM_METADATA_PERMISSION);
    }

    @Test
    public void deleteMetaWithMetaNotFound() throws IOException {
        DeleteMetaRequest deleteRequest = new DeleteMetaRequest(Long.MAX_VALUE);
        HttpResponse      metaResponse  = sendStandardRequest(UrlMapping.FOLDER__DELETE_META, deleteRequest);
        assertCinnamonError(metaResponse, ErrorCode.METASET_NOT_FOUND);
    }

    @Test
    public void deleteMetaHappyPathById() throws IOException {
        var meta = new TestObjectHolder(client, userId)
                .createFolder()
                .createFolderMeta("my meta").meta;
        client.deleteFolderMeta(meta.getId());
    }

    @Test
    public void updateFolderInvalidRequest() throws IOException {
        UpdateFolderRequest request = new UpdateFolderRequest();
        try {
            client.updateFolder(request);
        } catch (CinnamonClientException e) {
            assertEquals(e.getErrorCode(), ErrorCode.INVALID_REQUEST);
        }
    }

    @Test
    public void updateFolderFolderNotFound() throws IOException {
        UpdateFolderRequest request = new UpdateFolderRequest(
                Long.MAX_VALUE, null, null, null, null, null);
        try {
            client.updateFolder(request);
        } catch (CinnamonClientException e) {
            assertEquals(e.getErrorCode(), ErrorCode.FOLDER_NOT_FOUND);
        }
    }

    @Test
    public void updateFolderNoEditPermission() throws IOException {
        var folderId = prepareAclGroupWithPermissions(List.of()).createFolder().folder.getId();
        UpdateFolderRequest request = new UpdateFolderRequest(
                folderId, null, null, null, null, null);
        assertClientError(() -> client.updateFolder(request), NO_EDIT_FOLDER_PERMISSION);
    }

    @Test
    public void updateFolderNotWritable() throws IOException {
        var folderId = prepareAclGroupWithPermissions(List.of(CREATE_FOLDER, EDIT_FOLDER))
                .createFolder()
                .folder.getId();
        UpdateFolderRequest request = new UpdateFolderRequest(
                folderId, null, null, null, null, null);
        assertClientError(() -> client.updateFolder(request), NO_WRITE_SYS_METADATA_PERMISSION);
    }

    @Test
    public void updateFolderCannotMoveFolderIntoItself() throws IOException {
        var                 folderId = new TestObjectHolder(client, userId).createFolder().folder.getId();
        UpdateFolderRequest request  = new UpdateFolderRequest(folderId, folderId, null, null, null, null);
        assertClientError(() -> client.updateFolder(request), CANNOT_MOVE_FOLDER_INTO_ITSELF);
    }

    @Test
    public void updateFolderNoCreatePermission() throws IOException {
        var folder = prepareAclGroupWithPermissions(List.of(EDIT_FOLDER, WRITE_OBJECT_SYS_METADATA))
                .createFolder().createFolder().folder;
        UpdateFolderRequest request = new UpdateFolderRequest(
                folder.getId(), folder.getParentId(), null, null, null, null);
        assertClientError(() -> client.updateFolder(request), NO_CREATE_PERMISSION);
    }

    @Test
    public void updateFolderParentFolderNotFound() throws IOException {
        var folderId = new TestObjectHolder(client, userId)
                .createFolder().folder.getId();
        UpdateFolderRequest request = new UpdateFolderRequest(
                folderId, Long.MAX_VALUE, null, null, null, null);
        assertClientError(() -> client.updateFolder(request), PARENT_FOLDER_NOT_FOUND);
    }

    @Test
    public void updateFolderNoMovePermission() throws IOException {
        var targetFolder    = new TestObjectHolder(client, userId).createFolder().folder;
        var unmovableFolder = prepareAclGroupWithPermissions(List.of(EDIT_FOLDER, WRITE_OBJECT_SYS_METADATA)).createFolder().folder;
        UpdateFolderRequest request = new UpdateFolderRequest(
                unmovableFolder.getId(), targetFolder.getId(), null, null, null, null);
        assertClientError(() -> client.updateFolder(request), NO_MOVE_PERMISSION);
    }

    @Test
    public void updateFolderDuplicateFolderName() throws IOException {
        var folder = new TestObjectHolder(client, userId).createFolder().folder;
        UpdateFolderRequest request = new UpdateFolderRequest(
                folder.getId(), null, folder.getName(), null, null, null
        );
        assertClientError(() -> client.updateFolder(request), DUPLICATE_FOLDER_NAME_FORBIDDEN);
    }

    @Test
    public void updateFolderFolderTypeNotFound() throws IOException {
        var folder = new TestObjectHolder(client, userId).createFolder().folder;
        UpdateFolderRequest request = new UpdateFolderRequest(
                folder.getId(), null, null, null, Long.MAX_VALUE, null
        );
        assertClientError(() -> client.updateFolder(request), FOLDER_TYPE_NOT_FOUND);
    }

    @Test
    public void updateFolderMissingSetAclPermission() throws IOException {
        var folder = prepareAclGroupWithPermissions(List.of(CREATE_FOLDER, EDIT_FOLDER, WRITE_OBJECT_SYS_METADATA)).createFolder().folder;
        UpdateFolderRequest request = new UpdateFolderRequest(
                folder.getId(), null, null, null, null, 1L
        );
        assertClientError(() -> client.updateFolder(request), MISSING_SET_ACL_PERMISSION);
    }

    @Test
    public void updateFolderAclNotFound() throws IOException {
        var folder = new TestObjectHolder(client, userId).createFolder().folder;
        UpdateFolderRequest request = new UpdateFolderRequest(
                folder.getId(), null, null, null, null, Long.MAX_VALUE
        );
        assertClientError(() -> client.updateFolder(request), ACL_NOT_FOUND);
    }

    @Test
    public void updateFolderUserAccountNotFound() throws IOException {
        var folder = new TestObjectHolder(client, userId).createFolder().folder;
        UpdateFolderRequest request = new UpdateFolderRequest(
                folder.getId(), null, null, Long.MAX_VALUE, null, null
        );
        assertClientError(() -> client.updateFolder(request), USER_ACCOUNT_NOT_FOUND);
    }

    @Test
    public void updateFolderHappyPath() throws IOException {
        var targetFolderId = new TestObjectHolder(client, userId).createFolder().folder.getId();
        var adminToh = prepareAclGroupWithPermissions(List.of(EDIT_FOLDER, MOVE, BROWSE,
                WRITE_OBJECT_SYS_METADATA, SET_ACL))
                .createFolder();
        UpdateFolderRequest request = new UpdateFolderRequest(
                adminToh.folder.getId(), targetFolderId, "new-name-for-happy-folder", 1L, 2L,
                adminToh.acl.getId()
        );
        client.updateFolder(request);
        Folder updatedFolder = client.getFolderById(request.getId(), false);
        assertEquals(targetFolderId, updatedFolder.getParentId());
    }

    @Test
    public void getSubFoldersInvalidRequest() throws IOException {
        SingleFolderRequest request  = new SingleFolderRequest();
        HttpResponse        response = sendStandardRequest(UrlMapping.FOLDER__GET_SUBFOLDERS, request);
        assertCinnamonError(response, ErrorCode.INVALID_REQUEST);
    }

    @Test
    public void getSubFoldersFolderNotFound() throws IOException {
        SingleFolderRequest request  = new SingleFolderRequest(Long.MAX_VALUE, false);
        HttpResponse        response = sendStandardRequest(UrlMapping.FOLDER__GET_SUBFOLDERS, request);
        assertCinnamonError(response, ErrorCode.FOLDER_NOT_FOUND);
    }

    @Test
    public void getSubFoldersHappyPath() throws IOException {
        var toh            = new TestObjectHolder(client, userId);
        var parentFolderId = toh.createFolder().folder.getId();
        toh.createFolder("a", parentFolderId)
                .createFolder("b", parentFolderId)
                .createFolder("c", parentFolderId);
        List<Folder> folders     = client.getSubFolders(parentFolderId, false);
        Set<String>  folderNames = folders.stream().map(Folder::getName).collect(Collectors.toSet());
        assertTrue(folderNames.containsAll(Set.of("a", "b", "c")));
    }

    @Test
    public void createFolderInvalidRequest() throws IOException {
        CreateFolderRequest request  = new CreateFolderRequest();
        HttpResponse        response = sendStandardRequest(UrlMapping.FOLDER__CREATE, request);
        assertCinnamonError(response, ErrorCode.INVALID_REQUEST);
    }

    @Test
    public void createFolderNoParentFolder() throws IOException {
        CreateFolderRequest request  = new CreateFolderRequest("foo", Long.MAX_VALUE, "<sum/>", null, null, null);
        HttpResponse        response = sendStandardRequest(UrlMapping.FOLDER__CREATE, request);
        assertCinnamonError(response, ErrorCode.PARENT_FOLDER_NOT_FOUND);
    }

    @Test
    public void createFolderWithoutPermission() throws IOException {
        // trying to create a folder in root folder#1
        CreateFolderRequest request  = new CreateFolderRequest("foo", 1L, "<sum/>", null, null, null);
        HttpResponse        response = sendStandardRequest(UrlMapping.FOLDER__CREATE, request);
        assertCinnamonError(response, ErrorCode.NO_CREATE_PERMISSION);
    }

    @Test
    public void createFolderDuplicateName() throws IOException {
        var folder = new TestObjectHolder(client, userId)
                .createFolder().folder;
        assertClientError(() -> client.createFolder(createFolderId, folder.getName(), null, null, null),
                DUPLICATE_FOLDER_NAME_FORBIDDEN);
    }

    @Test
    public void createFolderWithTypeNotFound() throws IOException {
        CreateFolderRequest request  = new CreateFolderRequest("untyped folder", createFolderId, "<sum/>", null, null, Long.MAX_VALUE);
        HttpResponse        response = sendStandardRequest(UrlMapping.FOLDER__CREATE, request);
        assertCinnamonError(response, ErrorCode.FOLDER_TYPE_NOT_FOUND);
    }

    @Test
    public void createFolderWithoutValidUserAccount() throws IOException {
        CreateFolderRequest request  = new CreateFolderRequest("folder without owner", createFolderId, "<sum/>", Long.MAX_VALUE, null, null);
        HttpResponse        response = sendStandardRequest(UrlMapping.FOLDER__CREATE, request);
        assertCinnamonError(response, ErrorCode.USER_ACCOUNT_NOT_FOUND);
    }

    @Test
    public void createFolderWithoutValidAcl() throws IOException {
        CreateFolderRequest request  = new CreateFolderRequest("folder without acl", createFolderId, "<sum/>", null, Long.MAX_VALUE, null);
        HttpResponse        response = sendStandardRequest(UrlMapping.FOLDER__CREATE, request);
        assertCinnamonError(response, ErrorCode.ACL_NOT_FOUND);
    }

    @Test
    public void createFolderHappyPathInheriting() throws IOException {
        CreateFolderRequest request  = new CreateFolderRequest("create happy folder inherit", createFolderId, "<sum/>", null, null, null);
        HttpResponse        response = sendStandardRequest(UrlMapping.FOLDER__CREATE, request);
        List<Folder>        folders  = unwrapFolders(response, 1);
        Folder              folder   = folders.get(0);
        assertEquals("create happy folder inherit", folder.getName());
        assertThat("new folder must have id", folder.getId() > 0);
    }

    @Test
    public void createFolderHappyPath() throws IOException {
        CreateFolderRequest request  = new CreateFolderRequest("create happy folder", createFolderId, "<sum/>", 2L, 2L, 2L);
        HttpResponse        response = sendStandardRequest(UrlMapping.FOLDER__CREATE, request);
        List<Folder>        folders  = unwrapFolders(response, 1);
        Folder              folder   = folders.get(0);
        assertEquals("create happy folder", folder.getName());
        assertThat("new folder must have id", folder.getId() > 0);
    }

    @Test
    public void deleteFolderInvalidRequest() {
        CinnamonClientException exception = assertThrows(CinnamonClientException.class,
                () -> client.deleteFolder(Collections.emptyList(), false, false));
        assertEquals(INVALID_REQUEST, exception.getErrorCode());
    }

    @Test
    public void deleteFolderNoDeletePermission() throws IOException {
        TestObjectHolder toh = new TestObjectHolder(adminClient, userId);
        toh.createAcl("deleteFolderNoDeletePermission")
                .createFolder("unloeschbar", createFolderId);
        CinnamonClientException exception = assertThrows(CinnamonClientException.class, () -> client.deleteFolder(toh.folder.getId(), false, false));
        assertEquals(NO_DELETE_PERMISSION, exception.getErrorCode());
    }

    @Test
    public void createFolderWithUmlaut() throws IOException {
        TestObjectHolder toh = new TestObjectHolder(adminClient, userId);
        toh.createAcl("createFolderWithUmlaut")
                .createFolder("the földer", createFolderId);
    }

    @Test
    public void deleteNonExistentFolder() {
        assertClientError(() -> client.deleteFolder(List.of(Long.MAX_VALUE), false, false),
                FOLDER_NOT_FOUND);
    }

    @Test
    public void deleteEmptyFolder() throws IOException {
        var emptyFolder = prepareAclGroupWithOwnerPermissions(List.of(DefaultPermission.DELETE_FOLDER))
                .createFolder("deleteEmptyFolder", createFolderId)
                .folder;
        client.deleteFolder(emptyFolder.getId(), false, false);
        assertClientError(() -> client.getFolderById(emptyFolder.getId(), false),
                FOLDER_NOT_FOUND);
    }

    /**
     * Folder contains empty folders and deleteRecursively=false
     */
    @Test
    public void deleteFolderFailWithSubfolderNonRecursively() throws IOException {
        TestObjectHolder toh                 = prepareAclGroupWithOwnerPermissions(List.of(DefaultPermission.DELETE_FOLDER, DefaultPermission.CREATE_FOLDER));
        Folder           folderWithSubfolder = toh.createFolder("folderWithSubfolderFail", createFolderId).folder;
        client.createFolder(folderWithSubfolder.getId(), "subfolder", userId, toh.acl.getId(), folderWithSubfolder.getTypeId());

        assertClientError(() -> client.deleteFolder(folderWithSubfolder.getId(), false, false),
                FOLDER_HAS_SUBFOLDERS);
    }


    /**
     * Folder contains empty folders and deleteRecursively=true
     */
    @Test
    public void deleteFolderWithSubfoldersRecursively() throws IOException {
        TestObjectHolder toh                 = prepareAclGroupWithOwnerPermissions(List.of(DefaultPermission.DELETE_FOLDER, DefaultPermission.CREATE_FOLDER));
        Folder           folderWithSubfolder = toh.createFolder("folderWithSubfolder", createFolderId).folder;
        client.createFolder(folderWithSubfolder.getId(), "subfolder", userId, toh.acl.getId(), folderWithSubfolder.getTypeId());
        client.deleteFolder(folderWithSubfolder.getId(), true, false);
        var ex = assertThrows(CinnamonClientException.class, () -> client.getFolderById(folderWithSubfolder.getId(), false));
        assertEquals(FOLDER_NOT_FOUND, ex.getErrorCode());
    }

    /**
     * Folder contains OSD and deleteContent=false
     */
    @Test
    public void deleteFolderWithContentButWithoutDeleteContentParameterFail() throws IOException {
        TestObjectHolder toh = prepareAclGroupWithOwnerPermissions(List.of(DefaultPermission.DELETE_FOLDER))
                .createFolder("folderWithContentFail", createFolderId)
                .createOsd("undeleted");
        var ex = assertThrows(CinnamonClientException.class, () -> client.deleteFolder(toh.folder.getId(), false, false));
        assertEquals(FOLDER_IS_NOT_EMPTY, ex.getErrorCode());
    }

    /**
     * Folder contains OSD and deleteContent=true and OSD.acl allows it
     */
    @Test
    public void deleteFolderWithContentWithDeleteContentParameter() throws IOException {
        TestObjectHolder toh = prepareAclGroupWithPermissions(List.of(DefaultPermission.DELETE_FOLDER, DefaultPermission.DELETE_OBJECT))
                .createFolder("folderWithContent", createFolderId)
                .createOsd("delete-me");
        client.deleteFolder(toh.folder.getId(), false, true);
        assertClientError(() -> client.getFolderById(toh.folder.getId(), false), FOLDER_NOT_FOUND);
    }

    /**
     * Folder contains OSD and deleteContent=true and OSD.acl forbids it
     */
    @Test
    public void deleteFolderWithContentWithDeleteContentParameterButMissingPermission() throws IOException {
        TestObjectHolder toh = prepareAclGroupWithPermissions(List.of(DefaultPermission.DELETE_FOLDER))
                .createFolder("folderWithContentMissingPermission", createFolderId)
                .createOsd("delete-me");
        var ex = assertThrows(CinnamonClientException.class, () -> client.deleteFolder(toh.folder.getId(), false, true));
        assertEquals(CANNOT_DELETE_DUE_TO_ERRORS, ex.getErrorCode());
        assertTrue(ex.getErrorWrapper().getErrors().stream().anyMatch(e -> e.getCode().equals(NO_DELETE_PERMISSION.getCode())));
    }

    /**
     * Folder contains link with acl which prevents us from deleting the link
     */
    @Test
    public void deleteFolderWithLinksWithoutDeleteLinkAclPermission() throws IOException {
        TestObjectHolder toh = prepareAclGroupWithOwnerPermissions(List.of(DefaultPermission.DELETE_FOLDER))
                .createOsd("outside-of-deleteFolder-folder-as-link-target")
                .createFolder("deleteFolderWithLinksWithoutDeleteLinkAclPermission", createFolderId)
                .createLinkToOsd();
        // at the moment, links without delete permission will not return a list of errors
        //  assertEquals(CANNOT_DELETE_DUE_TO_ERRORS, ex.getErrorCode());
        assertClientError(() -> client.deleteFolder(toh.folder.getId(), false, true), NO_DELETE_LINK_PERMISSION);
    }

    /**
     * Folder contains link with acl that allows deletion of link
     */
    @Test
    public void deleteFolderWithLinksWithDeleteLinkAclPermission() throws IOException {
        TestObjectHolder toh = prepareAclGroupWithPermissions(List.of(DefaultPermission.DELETE_FOLDER, DefaultPermission.DELETE_OBJECT))
                .createOsd()
                .createFolder()
                .createLinkToOsd();
        client.deleteFolder(toh.folder.getId(), false, true);
    }

    /**
     * Folder has object content, and links from outside point to it.
     * As long as user is allowed to delete the content, the links should be removed automatically, no
     * matter their acl
     */
    @Test
    public void deleteFolderWithOutsideLinkToContainedObject() throws IOException {
        TestObjectHolder toh = prepareAclGroupWithPermissions(
                List.of(DefaultPermission.DELETE_FOLDER, DefaultPermission.DELETE_OBJECT))
                .createFolder()
                .createOsd("osd with link pointing to it");
        Folder folderWithOsdInside = toh.folder;
        toh.folder = creationFolder;
        toh.createLinkToOsd();
        client.deleteFolder(folderWithOsdInside.getId(), false, true);
        assertClientError(() -> client.getLinksById(List.of(toh.link.getId()), false), OBJECT_NOT_FOUND);
    }

    @Test
    public void deleteFolderWithContentProtectedByRelation() throws IOException {
        TestObjectHolder toh = prepareAclGroupWithPermissions(List.of(DefaultPermission.DELETE_FOLDER, DefaultPermission.DELETE_OBJECT))
                .createFolder("folder with relation source", createFolderId)
                .createOsd("relation-source");
        ObjectSystemData relationSource = toh.osd;
        toh.createFolder("delete-with-protected-relation", createFolderId)
                .createOsd("relation-target");
        ObjectSystemData relationTarget = toh.osd;
        RelationType relationType = adminClient.createRelationType(new RelationType("protected-relation", false, true,
                false, false, false, false));
        Relation relation = adminClient.createRelation(relationSource.getId(), relationTarget.getId(), relationType.getId(), "");
        var      ex       = assertThrows(CinnamonClientException.class, () -> client.deleteFolder(toh.folder.getId(), false, true));
        // at the moment, links without delete permission will not return a list of errors
        //  assertEquals(CANNOT_DELETE_DUE_TO_ERRORS, ex.getErrorCode());
        assertTrue(ex.getErrorWrapper().getErrors().stream().anyMatch(e -> e.getCode().equals(OBJECT_HAS_PROTECTED_RELATIONS.getCode())));
    }

    @Test
    public void deleteFolderWithMetadata() throws IOException {
        TestObjectHolder toh    = new TestObjectHolder(client, userId);
        Folder           folder = toh.createFolder("deleteFolderWithMetadata", createFolderId).folder;
        client.createFolderMeta(new CreateMetaRequest(folder.getId(), "some content", 1L));
        adminClient.deleteFolder(folder.getId(), false, false);
    }

    @Test
    public void deleteFolderWithContentWithUnprotectedRelation() throws IOException {
        TestObjectHolder toh = prepareAclGroupWithPermissions(
                List.of(DefaultPermission.DELETE_FOLDER, DefaultPermission.DELETE_OBJECT))
                .createFolder("folder with other relation source", createFolderId)
                .createOsd("relation-source");
        ObjectSystemData relationSource = toh.osd;
        toh.createFolder("delete-with-unprotected-relation", createFolderId)
                .createOsd("relation-target");
        ObjectSystemData relationTarget = toh.osd;
        RelationType relationType = adminClient.createRelationType(new RelationType("unprotected-relation", false, false,
                false, false, false, false));
        Relation relation = adminClient.createRelation(relationSource.getId(), relationTarget.getId(), relationType.getId(), "");
        client.deleteFolder(toh.folder.getId(), false, true);
    }

    private List<Folder> unwrapFolders(HttpResponse response, Integer expectedSize) throws IOException {
        assertResponseOkay(response);
        List<Folder> folders = mapper.readValue(response.getEntity().getContent(), FolderWrapper.class).getFolders();
        if (expectedSize != null) {
            assertNotNull(folders);
            assertFalse(folders.isEmpty());
            assertThat(folders.size(), equalTo(expectedSize));
        }
        return folders;
    }
}
