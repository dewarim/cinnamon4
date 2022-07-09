package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.DefaultPermission;
import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.client.CinnamonClientException;
import com.dewarim.cinnamon.model.Folder;
import com.dewarim.cinnamon.model.Meta;
import com.dewarim.cinnamon.model.ObjectSystemData;
import com.dewarim.cinnamon.model.links.Link;
import com.dewarim.cinnamon.model.links.LinkType;
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
import com.dewarim.cinnamon.model.response.MetaWrapper;
import com.dewarim.cinnamon.model.response.Summary;
import com.dewarim.cinnamon.model.response.SummaryWrapper;
import com.dewarim.cinnamon.test.TestObjectHolder;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Node;
import nu.xom.ParsingException;
import org.apache.http.HttpResponse;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.dewarim.cinnamon.ErrorCode.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class FolderServletIntegrationTest extends CinnamonIntegrationTest {

    @Test
    public void setSummaryHappyPath() throws IOException {
        SetSummaryRequest summaryRequest = new SetSummaryRequest(11L, "a summary");
        HttpResponse      response       = sendStandardRequest(UrlMapping.FOLDER__SET_SUMMARY, summaryRequest);
        assertResponseOkay(response);

        IdListRequest idListRequest  = new IdListRequest(Collections.singletonList(11L));
        HttpResponse  verifyResponse = sendStandardRequest(UrlMapping.FOLDER__GET_SUMMARIES, idListRequest);
        assertResponseOkay(verifyResponse);
        SummaryWrapper wrapper = mapper.readValue(verifyResponse.getEntity().getContent(), SummaryWrapper.class);
        assertThat(wrapper.getSummaries().get(0).getContent(), equalTo("a summary"));
    }

    @Test
    public void setSummaryMissingPermission() throws IOException {
        SetSummaryRequest summaryRequest = new SetSummaryRequest(12L, "a summary");
        HttpResponse      response       = sendStandardRequest(UrlMapping.FOLDER__SET_SUMMARY, summaryRequest);
        assertCinnamonError(response, ErrorCode.NO_WRITE_SYS_METADATA_PERMISSION);
    }

    @Test
    public void setSummaryMissingObject() throws IOException {
        SetSummaryRequest summaryRequest = new SetSummaryRequest(Long.MAX_VALUE, "a summary");
        HttpResponse      response       = sendStandardRequest(UrlMapping.FOLDER__SET_SUMMARY, summaryRequest);
        assertCinnamonError(response, ErrorCode.OBJECT_NOT_FOUND);
    }

    @Test
    public void getSummaryHappyPath() throws IOException {
        IdListRequest idListRequest = new IdListRequest(Collections.singletonList(13L));
        HttpResponse  response      = sendStandardRequest(UrlMapping.FOLDER__GET_SUMMARIES, idListRequest);
        assertResponseOkay(response);
        SummaryWrapper wrapper   = mapper.readValue(response.getEntity().getContent(), SummaryWrapper.class);
        List<Summary>  summaries = wrapper.getSummaries();
        assertNotNull(summaries);
        assertFalse(summaries.isEmpty());
        assertThat(wrapper.getSummaries().get(0).getContent(), equalTo("<sum>folder</sum>"));
    }

    @Test
    public void getSummariesMissingPermission() {
        var ex = assertThrows(CinnamonClientException.class, () -> client.getFolderSummaries(List.of(12L)));
        assertEquals(NO_READ_OBJECT_SYS_METADATA_PERMISSION, ex.getErrorCode());
    }

    @Test
    public void getFolderHappyPath() throws IOException {
        SingleFolderRequest singleRequest = new SingleFolderRequest(6L, false);
        HttpResponse        response      = sendStandardRequest(UrlMapping.FOLDER__GET_FOLDER, singleRequest);
        List<Folder>        folders       = unwrapFolders(response, 3);
        assertTrue(folders.stream().anyMatch(folder -> folder.getId().equals(6L)));
        assertTrue(folders.stream().anyMatch(folder -> folder.getId().equals(2L)));
        assertTrue(folders.stream().anyMatch(folder -> folder.getId().equals(1L)));
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
        FolderRequest folderRequest = new FolderRequest(Arrays.asList(6L, 2L), false);
        HttpResponse  response      = sendStandardRequest(UrlMapping.FOLDER__GET_FOLDERS, folderRequest);
        List<Folder>  folders       = unwrapFolders(response, 2);
        assertTrue(folders.stream().anyMatch(folder -> folder.getId().equals(6L)));
        assertTrue(folders.stream().anyMatch(folder -> folder.getId().equals(2L)));
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
        FolderPathRequest request     = new FolderPathRequest("/home/creation/some-sub-folder", true);
        HttpResponse      response    = sendStandardRequest(UrlMapping.FOLDER__GET_FOLDER_BY_PATH, request);
        List<Folder>      folders     = unwrapFolders(response, 4);
        List<String>      folderNames = folders.stream().map(Folder::getName).collect(Collectors.toList());
        assertTrue(folderNames.contains("root"));
        assertTrue(folderNames.contains("home"));
        assertTrue(folderNames.contains("creation"));
        assertTrue(folderNames.contains("some-sub-folder"));
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
        MetaRequest  request      = new MetaRequest(15L, null);
        HttpResponse metaResponse = sendStandardRequest(UrlMapping.FOLDER__GET_META, request);
        assertCinnamonError(metaResponse, ErrorCode.NO_READ_CUSTOM_METADATA_PERMISSION);
    }

    @Test
    public void getMetaHappyPath() throws IOException, ParsingException {
        MetaRequest  request      = new MetaRequest(16L, null);
        HttpResponse metaResponse = sendStandardRequest(UrlMapping.FOLDER__GET_META, request);
        assertResponseOkay(metaResponse);
        String   content = new String(metaResponse.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
        Document metaDoc = new Builder().build(content, null);
        Node     comment = metaDoc.query("//metasets/metaset[typeId/text()='1']/content").get(0);
        assertEquals("<metaset><p>Good Folder Meta Test</p></metaset>", comment.getValue());
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
            assertEquals(e.getErrorCode(), ErrorCode.FOLDER_NOT_FOUND);
        }
    }

    @Test
    public void createMetaObjectNotWritable() {
        CreateMetaRequest request = new CreateMetaRequest(15L, "foo", 1L);
        assertClientError(() -> client.createFolderMeta(request), NO_WRITE_CUSTOM_METADATA_PERMISSION);
    }

    @Test
    public void createMetaMetasetTypeByIdNotFound() {
        CreateMetaRequest request = new CreateMetaRequest(17L, "foo", Long.MAX_VALUE);
        assertClientError(() -> client.createFolderMeta(request), METASET_TYPE_NOT_FOUND);
    }

    @Test
    public void createMetaMetasetTypeByNameNotFound() {
        CreateMetaRequest request = new CreateMetaRequest(17L, "foo", "unknown");
        assertClientError(() -> client.createFolderMeta(request), METASET_TYPE_NOT_FOUND);
    }

    @Test
    public void createMetaMetasetIsUniqueAndExists() {
        CreateMetaRequest request = new CreateMetaRequest(18L, "duplicate license", "license");
        assertClientError(() -> client.createFolderMeta(request), METASET_IS_UNIQUE_AND_ALREADY_EXISTS);
    }

    @Test
    public void createFolderMetasetHappyWithExistingMeta() throws IOException {
        CreateMetaRequest request      = new CreateMetaRequest(19L, "duplicate comment", "comment");
        HttpResponse      metaResponse = sendStandardRequest(UrlMapping.FOLDER__CREATE_META, request);
        assertResponseOkay(metaResponse);
        MetaRequest  metaRequest     = new MetaRequest(19L, Collections.singletonList("comment"));
        HttpResponse commentResponse = sendStandardRequest(UrlMapping.FOLDER__GET_META, metaRequest);
        assertResponseOkay(commentResponse);
        MetaWrapper metaWrapper = mapper.readValue(commentResponse.getEntity().getContent(), MetaWrapper.class);
        assertEquals(2, metaWrapper.getMetasets().size());
    }

    @Test
    public void createOsdMetasetHappyPath() throws IOException {
        CreateMetaRequest request      = new CreateMetaRequest(17L, "new license meta", "license");
        HttpResponse      metaResponse = sendStandardRequest(UrlMapping.OSD__CREATE_META, request);
        assertResponseOkay(metaResponse);
        MetaWrapper metaWrapper = mapper.readValue(metaResponse.getEntity().getContent(), MetaWrapper.class);
        assertEquals(1, metaWrapper.getMetasets().size());
        Meta meta = metaWrapper.getMetasets().get(0);
        assertEquals("new license meta", meta.getContent());
        assertEquals(2, meta.getTypeId().longValue());
    }


    @Test
    public void deleteMetaInvalidRequest() throws IOException {
        DeleteMetaRequest deleteRequest = new DeleteMetaRequest();
        HttpResponse      metaResponse  = sendStandardRequest(UrlMapping.FOLDER__DELETE_META, deleteRequest);
        assertCinnamonError(metaResponse, ErrorCode.INVALID_REQUEST);
    }

    @Test
    public void deleteMetaObjectNotFound() throws IOException {
        DeleteMetaRequest deleteRequest = new DeleteMetaRequest(Long.MAX_VALUE, 1L);
        HttpResponse      metaResponse  = sendStandardRequest(UrlMapping.FOLDER__DELETE_META, deleteRequest);
        assertCinnamonError(metaResponse, ErrorCode.FOLDER_NOT_FOUND);
    }

    @Test
    public void deleteMetaWithoutPermission() throws IOException {
        DeleteMetaRequest deleteRequest = new DeleteMetaRequest(20L, "comment");
        HttpResponse      metaResponse  = sendStandardRequest(UrlMapping.FOLDER__DELETE_META, deleteRequest);
        assertCinnamonError(metaResponse, NO_WRITE_CUSTOM_METADATA_PERMISSION);
    }

    @Test
    public void deleteMetaWithMetaNotFound() throws IOException {
        DeleteMetaRequest deleteRequest = new DeleteMetaRequest(21L, "unknown-type");
        HttpResponse      metaResponse  = sendStandardRequest(UrlMapping.FOLDER__DELETE_META, deleteRequest);
        assertCinnamonError(metaResponse, ErrorCode.METASET_NOT_FOUND);
    }

    @Test
    public void deleteMetaHappyPathById() throws IOException {
        // #7 folder_meta = metaset_type license
        client.deleteFolderMeta(21L, 7L);
    }

    @Test
    public void deleteMetaHappyPathByName() throws IOException {
        // #5 + #6 folder_meta = metaset_type comment
        client.deleteFolderMeta(21L, "comment");
    }

    @Test
    public void updateFolderInvalidRequest() throws IOException {
        UpdateFolderRequest request = new UpdateFolderRequest();
        try {
            client.updateFolders(request);
        } catch (CinnamonClientException e) {
            assertEquals(e.getErrorCode(), ErrorCode.INVALID_REQUEST);
        }
    }

    @Test
    public void updateFolderFolderNotFound() throws IOException {
        UpdateFolderRequest request = new UpdateFolderRequest(
                Long.MAX_VALUE, null, null, null, null, null);
        try {
            client.updateFolders(request);
        } catch (CinnamonClientException e) {
            assertEquals(e.getErrorCode(), ErrorCode.FOLDER_NOT_FOUND);
        }
    }

    @Test
    public void updateFolderNoEditPermission() throws IOException {
        UpdateFolderRequest request = new UpdateFolderRequest(
                22L, null, null, null, null, null);
        try {
            client.updateFolders(request);
        } catch (CinnamonClientException e) {
            assertEquals(e.getErrorCode(), ErrorCode.NO_EDIT_FOLDER_PERMISSION);
        }
    }

    @Test
    public void updateFolderNotWritable() throws IOException {
        UpdateFolderRequest request = new UpdateFolderRequest(
                26L, null, null, null, null, null);
        try {
            client.updateFolders(request);
        } catch (CinnamonClientException e) {
            assertEquals(e.getErrorCode(), ErrorCode.NO_WRITE_SYS_METADATA_PERMISSION);
        }
    }

    @Test
    public void updateFolderCannotMoveFolderIntoItself() throws IOException {
        UpdateFolderRequest request = new UpdateFolderRequest(25L, 25L, null, null, null, null);
        try {
            client.updateFolders(request);
        } catch (CinnamonClientException e) {
            assertEquals(e.getErrorCode(), ErrorCode.CANNOT_MOVE_FOLDER_INTO_ITSELF);
        }
    }

    @Test
    public void updateFolderNoCreatePermission() throws IOException {
        UpdateFolderRequest request = new UpdateFolderRequest(
                25L, 22L, null, null, null, null);
        try {
            client.updateFolders(request);
        } catch (CinnamonClientException e) {
            assertEquals(e.getErrorCode(), ErrorCode.NO_CREATE_PERMISSION);
        }
    }

    @Test
    public void updateFolderParentFolderNotFound() throws IOException {
        UpdateFolderRequest request = new UpdateFolderRequest(
                25L, Long.MAX_VALUE, null, null, null, null);
        try {
            client.updateFolders(request);
        } catch (CinnamonClientException e) {
            assertEquals(e.getErrorCode(), ErrorCode.PARENT_FOLDER_NOT_FOUND);
        }
    }

    @Test
    public void updateFolderNoMovePermission() throws IOException {
        UpdateFolderRequest request = new UpdateFolderRequest(
                28L, 27L, null, null, null, null);
        try {
            client.updateFolders(request);
        } catch (CinnamonClientException e) {
            assertEquals(e.getErrorCode(), ErrorCode.NO_MOVE_PERMISSION);
        }
    }

    @Test
    public void updateFolderDuplicateFolderName() throws IOException {
        UpdateFolderRequest request = new UpdateFolderRequest(
                25L, null, "move here", null, null, null
        );
        try {
            client.updateFolders(request);
        } catch (CinnamonClientException e) {
            assertEquals(e.getErrorCode(), ErrorCode.DUPLICATE_FOLDER_NAME_FORBIDDEN);
        }
    }

    @Test
    public void updateFolderFolderTypeNotFound() throws IOException {
        UpdateFolderRequest request = new UpdateFolderRequest(
                25L, null, null, null, Long.MAX_VALUE, null
        );
        try {
            client.updateFolders(request);
        } catch (CinnamonClientException e) {
            assertEquals(e.getErrorCode(), ErrorCode.FOLDER_TYPE_NOT_FOUND);
        }
    }

    @Test
    public void updateFolderMissingSetAclPermission() throws IOException {
        UpdateFolderRequest request = new UpdateFolderRequest(
                23L, null, null, null, null, 1L
        );

        try {
            client.updateFolders(request);
        } catch (CinnamonClientException e) {
            assertEquals(e.getErrorCode(), ErrorCode.MISSING_SET_ACL_PERMISSION);
        }
    }

    @Test
    public void updateFolderAclNotFound() throws IOException {
        UpdateFolderRequest request = new UpdateFolderRequest(
                24L, null, null, null, null, Long.MAX_VALUE
        );

        try {
            client.updateFolders(request);
        } catch (CinnamonClientException e) {
            assertEquals(e.getErrorCode(), ErrorCode.ACL_NOT_FOUND);
        }
    }

    @Test
    public void updateFolderUserAccountNotFound() throws IOException {
        UpdateFolderRequest request = new UpdateFolderRequest(
                23L, null, null, Long.MAX_VALUE, null, null
        );
        try {
            client.updateFolders(request);
        } catch (CinnamonClientException e) {
            assertEquals(e.getErrorCode(), ErrorCode.USER_ACCOUNT_NOT_FOUND);
        }
    }

    @Test
    public void updateFolderHappyPath() throws IOException {
        UpdateFolderRequest request = new UpdateFolderRequest(
                25L, 27L, "new-name-for-happy-folder", 1L, 2L, 13L
        );
        HttpResponse response = sendStandardRequest(UrlMapping.FOLDER__UPDATE, request);
        parseGenericResponse(response);
        Folder updatedFolder = adminClient.getFolderById(25L, false);
        assertEquals(27L, updatedFolder.getParentId());
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
        // fetch sub folders in home folder #2
        SingleFolderRequest request  = new SingleFolderRequest(2L, false);
        HttpResponse        response = sendStandardRequest(UrlMapping.FOLDER__GET_SUBFOLDERS, request);
        List<Folder>        folders  = unwrapFolders(response, 3);
        assertTrue(folders.stream().map(Folder::getName).anyMatch(n -> n.equals("archive")));
        assertTrue(folders.stream().map(Folder::getName).anyMatch(n -> n.equals("creation")));
        assertTrue(folders.stream().map(Folder::getName).anyMatch(n -> n.equals("deletion")));

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
        CreateFolderRequest request  = new CreateFolderRequest("duplicate name", 6L, "<sum/>", null, null, null);
        HttpResponse        response = sendStandardRequest(UrlMapping.FOLDER__CREATE, request);
        assertCinnamonError(response, ErrorCode.DUPLICATE_FOLDER_NAME_FORBIDDEN);
    }

    @Test
    public void createFolderWithTypeNotFound() throws IOException {
        CreateFolderRequest request  = new CreateFolderRequest("untyped folder", 6L, "<sum/>", null, null, Long.MAX_VALUE);
        HttpResponse        response = sendStandardRequest(UrlMapping.FOLDER__CREATE, request);
        assertCinnamonError(response, ErrorCode.FOLDER_TYPE_NOT_FOUND);
    }

    @Test
    public void createFolderWithoutValidUserAccount() throws IOException {
        CreateFolderRequest request  = new CreateFolderRequest("folder without owner", 6L, "<sum/>", Long.MAX_VALUE, null, null);
        HttpResponse        response = sendStandardRequest(UrlMapping.FOLDER__CREATE, request);
        assertCinnamonError(response, ErrorCode.USER_ACCOUNT_NOT_FOUND);
    }

    @Test
    public void createFolderWithoutValidAcl() throws IOException {
        CreateFolderRequest request  = new CreateFolderRequest("folder without acl", 6L, "<sum/>", null, Long.MAX_VALUE, null);
        HttpResponse        response = sendStandardRequest(UrlMapping.FOLDER__CREATE, request);
        assertCinnamonError(response, ErrorCode.ACL_NOT_FOUND);
    }

    @Test
    public void createFolderHappyPathInheriting() throws IOException {
        CreateFolderRequest request  = new CreateFolderRequest("create happy folder inherit", 6L, "<sum/>", null, null, null);
        HttpResponse        response = sendStandardRequest(UrlMapping.FOLDER__CREATE, request);
        List<Folder>        folders  = unwrapFolders(response, 1);
        Folder              folder   = folders.get(0);
        assertEquals("create happy folder inherit", folder.getName());
        assertThat("new folder must have id", folder.getId() > 0);
    }

    @Test
    public void createFolderHappyPath() throws IOException {
        CreateFolderRequest request  = new CreateFolderRequest("create happy folder", 6L, "<sum/>", 2L, 2L, 2L);
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
        TestObjectHolder toh = new TestObjectHolder(adminClient, null, userId, createFolderId);
        toh.createAcl("deleteFolderNoDeletePermission")
                .createFolder("unloeschbar", createFolderId);
        CinnamonClientException exception = assertThrows(CinnamonClientException.class, () -> client.deleteFolder(toh.folder.getId(), false, false));
        assertEquals(NO_DELETE_PERMISSION, exception.getErrorCode());
    }

    @Test
    public void createFolderWithUmlaut() throws IOException {
        TestObjectHolder toh = new TestObjectHolder(adminClient, null, userId, createFolderId);
        toh.createAcl("createFolderWithUmlaut")
                .createFolder("the földer", createFolderId);
    }

    @Test
    public void deleteNonExistentFolder() {
        CinnamonClientException exception = assertThrows(CinnamonClientException.class,
                () -> client.deleteFolder(List.of(Long.MAX_VALUE), false, false));
        assertEquals(FOLDER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    public void deleteEmptyFolder() throws IOException {
        TestObjectHolder toh = new TestObjectHolder(adminClient, null, userId, createFolderId);
        toh.createAcl("delete-folder-allowed")
                .createGroup("deleteFolderUsers")
                .createAclGroup()
                .addPermissionsByName(List.of(DefaultPermission.DELETE_FOLDER.getName()))
                .addUserToGroup(userId);
        Folder emptyFolder = toh.createFolder("deleteEmptyFolder", createFolderId).folder;
        client.deleteFolder(emptyFolder.getId(), false, false);
        var ex = assertThrows(CinnamonClientException.class, () -> client.getFolderById(emptyFolder.getId(), false));
        assertEquals(FOLDER_NOT_FOUND, ex.getErrorCode());
    }

    /**
     * Folder contains empty folders and deleteRecursively=false
     */
    @Test
    public void deleteFolderFailWithSubfolderNonRecursively() throws IOException {
        TestObjectHolder toh = new TestObjectHolder(adminClient, null, userId, createFolderId);
        toh.createAcl("deleteFolderFailWithSubfolderNonRecursively")
                .createGroup("deleteFolderFailWithSubfolderNonRecursively")
                .createAclGroup()
                .addPermissionsByName(List.of(DefaultPermission.DELETE_FOLDER.getName(), DefaultPermission.CREATE_FOLDER.getName()))
                .addUserToGroup(userId);
        Folder folderWithSubfolder = toh.createFolder("folderWithSubfolderFail", createFolderId).folder;
        client.createFolder(folderWithSubfolder.getId(), "subfolder", userId, toh.acl.getId(), folderWithSubfolder.getTypeId());

        var ex = assertThrows(CinnamonClientException.class, () -> client.deleteFolder(folderWithSubfolder.getId(), false, false));
        assertEquals(FOLDER_HAS_SUBFOLDERS, ex.getErrorCode());
    }


    /**
     * Folder contains empty folders and deleteRecursively=true
     */
    @Test
    public void deleteFolderWithSubfoldersRecursively() throws IOException {
        TestObjectHolder toh = new TestObjectHolder(adminClient, null, userId, createFolderId);
        toh.createAcl("deleteFolderWithSubfoldersRecursively")
                .createGroup("deleteFolderWithSubfoldersRecursively")
                .createAclGroup()
                .addPermissionsByName(List.of(DefaultPermission.DELETE_FOLDER.getName(), DefaultPermission.CREATE_FOLDER.getName()))
                .addUserToGroup(userId);
        Folder folderWithSubfolder = toh.createFolder("folderWithSubfolder", createFolderId).folder;
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
        TestObjectHolder toh = new TestObjectHolder(adminClient, null, userId, createFolderId);
        toh.createAcl("deleteFolderWithContentFail")
                .createGroup("deleteFolderWithContentFail")
                .createAclGroup()
                .addPermissionsByName(List.of(DefaultPermission.DELETE_FOLDER.getName()))
                .addUserToGroup(userId)
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
        TestObjectHolder toh = new TestObjectHolder(adminClient, null, userId, createFolderId);
        toh.createAcl("deleteFolderWithContentWithDeleteContentParameter")
                .createGroup("deleteFolderWithContentWithDeleteContentParameter")
                .createAclGroup()
                .addPermissions(List.of(DefaultPermission.DELETE_FOLDER, DefaultPermission.DELETE_OBJECT))
                .addUserToGroup(userId)
                .createFolder("folderWithContent", createFolderId)
                .createOsd("delete-me");
        client.deleteFolder(toh.folder.getId(), false, true);
        var ex = assertThrows(CinnamonClientException.class, () -> client.getFolderById(toh.folder.getId(), false));
        assertEquals(FOLDER_NOT_FOUND, ex.getErrorCode());
    }

    /**
     * Folder contains OSD and deleteContent=true and OSD.acl forbids it
     */
    @Test
    public void deleteFolderWithContentWithDeleteContentParameterButMissingPermission() throws IOException {
        TestObjectHolder toh = new TestObjectHolder(adminClient, null, userId, createFolderId);
        toh.createAcl("deleteFolderWithContentWithDeleteContentParameterButMissingPermission")
                .createGroup("deleteFolderWithContentWithDeleteContentParameterButMissingPermission")
                .createAclGroup()
                .addPermissions(List.of(DefaultPermission.DELETE_FOLDER))
                .addUserToGroup(userId)
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
        TestObjectHolder toh = new TestObjectHolder(adminClient, null, userId, createFolderId);
        toh.createAcl("deleteFolderWithLinksWithoutDeleteLinkAclPermission")
                .createOsd("outside-of-deleteFolder-folder-as-link-target")
                .createGroup("deleteFolderWithLinksWithoutDeleteLinkAclPermission")
                .createAclGroup()
                .addPermissions(List.of(DefaultPermission.DELETE_FOLDER))
                .addUserToGroup(userId)
                .createFolder("deleteFolderWithLinksWithoutDeleteLinkAclPermission", createFolderId)
                .createLinkToOsd(toh.osd);
        var ex = assertThrows(CinnamonClientException.class, () -> client.deleteFolder(toh.folder.getId(), false, true));
        // at the moment, links without delete permission will not return a list of errors
        //  assertEquals(CANNOT_DELETE_DUE_TO_ERRORS, ex.getErrorCode());
        assertTrue(ex.getErrorWrapper().getErrors().stream().anyMatch(e -> e.getCode().equals(NO_DELETE_LINK_PERMISSION.getCode())));
    }

    /**
     * Folder contains link with acl that allows deletion of link
     */
    @Test
    public void deleteFolderWithLinksWithDeleteLinkAclPermission() throws IOException {
        TestObjectHolder toh = new TestObjectHolder(adminClient, null, userId, createFolderId);
        toh.createAcl("deleteFolderWithLinksWithDeleteLinkAclPermission")
                .createOsd("outside-of-deleteFolder-folder-as-link-target")
                .createGroup("deleteFolderWithLinksWithDeleteLinkAclPermission")
                .createAclGroup()
                .addPermissions(List.of(DefaultPermission.DELETE_FOLDER, DefaultPermission.DELETE_OBJECT))
                .addUserToGroup(userId)
                .createFolder("deleteFolderWithLinksWithDeleteLinkAclPermission", createFolderId)
                .createLinkToOsd(toh.osd);
        client.deleteFolder(toh.folder.getId(), false, true);
    }

    /**
     * Folder has object content, and links from outside point to it.
     * As long as user is allowed to delete the content, the links should be removed automatically, no
     * matter their acl
     */
    @Test
    public void deleteFolderWithOutsideLinkToContainedObject() throws IOException {
        TestObjectHolder toh = new TestObjectHolder(adminClient, null, userId, createFolderId);
        toh.createAcl("delete-with-outside-link")
                .createGroup("delete-with-outside-link")
                .createAclGroup()
                .addPermissions(List.of(DefaultPermission.DELETE_FOLDER, DefaultPermission.DELETE_OBJECT))
                .createFolder("delete-with-outside-link", createFolderId)
                .createOsd("delete-with-outside-link")
                .addUserToGroup(userId);
        Link link = adminClient.createLink(createFolderId, LinkType.OBJECT, toh.acl.getId(), userId, null, toh.osd.getId());
        client.deleteFolder(toh.folder.getId(), false, true);
        CinnamonClientException ce = assertThrows(CinnamonClientException.class, () -> client.getLinksById(List.of(link.getId()), false));
        assertEquals(OBJECT_NOT_FOUND, ce.getErrorCode());
    }

    @Test
    public void deleteFolderWithContentProtectedByRelation() throws IOException {
        TestObjectHolder toh = new TestObjectHolder(adminClient, null, userId, createFolderId);
        toh.createAcl("delete-with-protected-relation")
                .createGroup("delete-with-protected-relation")
                .createAclGroup()
                .addPermissions(List.of(DefaultPermission.DELETE_FOLDER, DefaultPermission.DELETE_OBJECT))
                .createFolder("folder with relation source", createFolderId)
                .addUserToGroup(userId)
                .createOsd("relation-source");
        ObjectSystemData relationSource = toh.osd;
        toh.createFolder("delete-with-protected-relation", createFolderId)
                .createOsd("relation-target");
        ObjectSystemData relationTarget = toh.osd;
        RelationType     relationType   = adminClient.createRelationType(new RelationType("protected-relation", false, true,
                false, false, false, false));
        Relation         relation       = adminClient.createRelation(relationSource.getId(), relationTarget.getId(), relationType.getId(), "");
        var ex = assertThrows(CinnamonClientException.class, () -> client.deleteFolder(toh.folder.getId(), false, true));
        // at the moment, links without delete permission will not return a list of errors
        //  assertEquals(CANNOT_DELETE_DUE_TO_ERRORS, ex.getErrorCode());
        assertTrue(ex.getErrorWrapper().getErrors().stream().anyMatch(e -> e.getCode().equals(OBJECT_HAS_PROTECTED_RELATIONS.getCode())));
    }

    @Test
    public void deleteFolderWithMetadata() throws IOException{
        TestObjectHolder toh = new TestObjectHolder(client, "reviewers.acl", userId, createFolderId);
        Folder folder = toh.createFolder("deleteFolderWithMetadata", createFolderId).folder;
        client.createFolderMeta(new CreateMetaRequest(folder.getId(), "some content", "comment"));
        adminClient.deleteFolder(folder.getId(), false, false);
    }

    @Test
    public void deleteFolderWithContentWithUnprotectedRelation() throws IOException {
        TestObjectHolder toh = new TestObjectHolder(adminClient, null, userId, createFolderId);
        toh.createAcl("delete-with-unprotected-relation")
                .createGroup("delete-with-unprotected-relation")
                .createAclGroup()
                .addPermissions(List.of(DefaultPermission.DELETE_FOLDER, DefaultPermission.DELETE_OBJECT))
                .createFolder("folder with other relation source", createFolderId)
                .addUserToGroup(userId)
                .createOsd("relation-source");
        ObjectSystemData relationSource = toh.osd;
        toh.createFolder("delete-with-unprotected-relation", createFolderId)
                .createOsd("relation-target");
        ObjectSystemData relationTarget = toh.osd;
        RelationType     relationType   = adminClient.createRelationType(new RelationType("unprotected-relation", false, false,
                false, false, false, false));
        Relation         relation       = adminClient.createRelation(relationSource.getId(), relationTarget.getId(), relationType.getId(), "");
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
