package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.application.ErrorCode;
import com.dewarim.cinnamon.application.UrlMapping;
import com.dewarim.cinnamon.model.Folder;
import com.dewarim.cinnamon.model.Meta;
import com.dewarim.cinnamon.model.request.*;
import com.dewarim.cinnamon.model.response.FolderWrapper;
import com.dewarim.cinnamon.model.response.MetaWrapper;
import com.dewarim.cinnamon.model.response.SummaryWrapper;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Node;
import nu.xom.ParsingException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

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
        assertThat(wrapper.getSummaries().get(0), equalTo("a summary"));
    }

    @Test
    public void setSummaryMissingPermission() throws IOException {
        SetSummaryRequest summaryRequest = new SetSummaryRequest(12L, "a summary");
        HttpResponse      response       = sendStandardRequest(UrlMapping.FOLDER__SET_SUMMARY, summaryRequest);
        assertCinnamonError(response, ErrorCode.NO_WRITE_SYS_METADATA_PERMISSION, HttpStatus.SC_FORBIDDEN);
    }

    @Test
    public void setSummaryMissingObject() throws IOException {
        SetSummaryRequest summaryRequest = new SetSummaryRequest(Long.MAX_VALUE, "a summary");
        HttpResponse      response       = sendStandardRequest(UrlMapping.FOLDER__SET_SUMMARY, summaryRequest);
        assertCinnamonError(response, ErrorCode.OBJECT_NOT_FOUND, SC_NOT_FOUND);
    }

    @Test
    public void getSummaryHappyPath() throws IOException {
        IdListRequest idListRequest = new IdListRequest(Collections.singletonList(13L));
        HttpResponse  response      = sendStandardRequest(UrlMapping.FOLDER__GET_SUMMARIES, idListRequest);
        assertResponseOkay(response);
        SummaryWrapper wrapper   = mapper.readValue(response.getEntity().getContent(), SummaryWrapper.class);
        List<String>   summaries = wrapper.getSummaries();
        assertNotNull(summaries);
        assertFalse(summaries.isEmpty());
        assertThat(wrapper.getSummaries().get(0), equalTo("<sum>folder</sum>"));
    }

    @Test
    public void getSummariesMissingPermission() throws IOException {
        IdListRequest idListRequest = new IdListRequest(Collections.singletonList(12L));
        HttpResponse  response      = sendStandardRequest(UrlMapping.FOLDER__GET_SUMMARIES, idListRequest);
        assertResponseOkay(response);
        SummaryWrapper wrapper = mapper.readValue(response.getEntity().getContent(), SummaryWrapper.class);
        // when all ids are non-readable, return an empty list:
        assertNull(wrapper.getSummaries());
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
        assertCinnamonError(response, ErrorCode.OBJECT_NOT_FOUND, SC_NOT_FOUND);
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
        assertCinnamonError(response, ErrorCode.OBJECT_NOT_FOUND, SC_NOT_FOUND);
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
        assertCinnamonError(response, ErrorCode.INVALID_FOLDER_PATH_STRUCTURE, SC_BAD_REQUEST);

        // trailing "/" is not allowed
        FolderPathRequest trailingRequest  = new FolderPathRequest("http://", true);
        HttpResponse      trailingResponse = sendStandardRequest(UrlMapping.FOLDER__GET_FOLDER_BY_PATH, trailingRequest);
        assertCinnamonError(trailingResponse, ErrorCode.INVALID_FOLDER_PATH_STRUCTURE, SC_BAD_REQUEST);
    }

    @Test
    public void getFolderByPathFolderNotFound() throws IOException {
        FolderPathRequest request  = new FolderPathRequest("/foo/bar", true);
        HttpResponse      response = sendStandardRequest(UrlMapping.FOLDER__GET_FOLDER_BY_PATH, request);
        assertCinnamonError(response, ErrorCode.OBJECT_NOT_FOUND, SC_NOT_FOUND);
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
    public void getMetaInvalidRequest() throws IOException{
        MetaRequest  request      = new MetaRequest();
        HttpResponse metaResponse = sendStandardRequest(UrlMapping.FOLDER__GET_META, request);
        assertCinnamonError(metaResponse, ErrorCode.INVALID_REQUEST);
    }

    @Test
    public void getMetaObjectNotFound() throws IOException{
        MetaRequest  request      = new MetaRequest(Long.MAX_VALUE, null);
        HttpResponse metaResponse = sendStandardRequest(UrlMapping.FOLDER__GET_META, request);
        assertCinnamonError(metaResponse, ErrorCode.OBJECT_NOT_FOUND, SC_NOT_FOUND);
    }

    @Test
    public void getMetaWithoutReadPermission() throws IOException{
        MetaRequest  request      = new MetaRequest(15L, null);
        HttpResponse metaResponse = sendStandardRequest(UrlMapping.FOLDER__GET_META, request);
        assertCinnamonError(metaResponse, ErrorCode.NO_READ_CUSTOM_METADATA_PERMISSION, SC_UNAUTHORIZED);
    }

    @Test
    public void getMetaHappyPath() throws IOException, ParsingException {
        MetaRequest  request      = new MetaRequest(16L, null);
        HttpResponse metaResponse = sendStandardRequest(UrlMapping.FOLDER__GET_META, request);
        assertResponseOkay(metaResponse);
        String   content = new String(metaResponse.getEntity().getContent().readAllBytes(), Charset.forName("UTF-8"));
        Document metaDoc = new Builder().build(content, null);
        Node     comment = metaDoc.query("//metasets/metaset[typeId/text()='1']/content").get(0);
        assertEquals("<metaset><p>Good Folder Meta Test</p></metaset>",comment.getValue());
    }

    @Test
    public void createMetaInvalidRequest() throws IOException {
        CreateMetaRequest request      = new CreateMetaRequest();
        HttpResponse      metaResponse = sendStandardRequest(UrlMapping.FOLDER__CREATE_META, request);
        assertCinnamonError(metaResponse, ErrorCode.INVALID_REQUEST);
    }

    @Test
    public void createMetaObjectNotFound() throws IOException {
        CreateMetaRequest request      = new CreateMetaRequest(Long.MAX_VALUE, "foo", 1L);
        HttpResponse      metaResponse = sendStandardRequest(UrlMapping.FOLDER__CREATE_META, request);
        assertCinnamonError(metaResponse, ErrorCode.FOLDER_NOT_FOUND, SC_NOT_FOUND);
    }

    @Test
    public void createMetaObjectNotWritable() throws IOException {
        CreateMetaRequest request      = new CreateMetaRequest(15L, "foo", 1L);
        HttpResponse      metaResponse = sendStandardRequest(UrlMapping.FOLDER__CREATE_META, request);
        assertCinnamonError(metaResponse, ErrorCode.NO_WRITE_CUSTOM_METADATA_PERMISSION, SC_UNAUTHORIZED);
    }

    @Test
    public void createMetaMetasetTypeByIdNotFound() throws IOException {
        CreateMetaRequest request      = new CreateMetaRequest(17L, "foo", Long.MAX_VALUE);
        HttpResponse      metaResponse = sendStandardRequest(UrlMapping.FOLDER__CREATE_META, request);
        assertCinnamonError(metaResponse, ErrorCode.METASET_TYPE_NOT_FOUND, SC_NOT_FOUND);
    }

    @Test
    public void createMetaMetasetTypeByNameNotFound() throws IOException {
        CreateMetaRequest request      = new CreateMetaRequest(17L, "foo", "unknown");
        HttpResponse      metaResponse = sendStandardRequest(UrlMapping.FOLDER__CREATE_META, request);
        assertCinnamonError(metaResponse, ErrorCode.METASET_TYPE_NOT_FOUND, SC_NOT_FOUND);
    }

    @Test
    public void createMetaMetasetIsUniqueAndExists() throws IOException {
        CreateMetaRequest request      = new CreateMetaRequest(18L, "duplicate license", "license");
        HttpResponse      metaResponse = sendStandardRequest(UrlMapping.FOLDER__CREATE_META, request);
        assertCinnamonError(metaResponse, ErrorCode.METASET_IS_UNIQUE_AND_ALREADY_EXISTS, SC_BAD_REQUEST);
    }

    @Test
    public void createMetaMetasetHappyWithExistingMeta() throws IOException {
        CreateMetaRequest request      = new CreateMetaRequest(19L, "duplicate comment", "comment");
        HttpResponse      metaResponse = sendStandardRequest(UrlMapping.FOLDER__CREATE_META, request);
        assertResponseOkay(metaResponse);
        MetaRequest  metaRequest      = new MetaRequest(19L, Collections.singletonList("comment"));
        HttpResponse commentResponse = sendStandardRequest(UrlMapping.FOLDER__GET_META, metaRequest);
        assertResponseOkay(commentResponse);
        MetaWrapper metaWrapper = mapper.readValue(commentResponse.getEntity().getContent(), MetaWrapper.class);
        assertEquals(2, metaWrapper.getMetasets().size());
    }

    @Test
    public void createMetaMetasetHappyPath() throws IOException {
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
        assertCinnamonError(metaResponse, ErrorCode.FOLDER_NOT_FOUND, SC_NOT_FOUND);
    }

    @Test
    public void deleteMetaWithoutPermission() throws IOException {
        DeleteMetaRequest deleteRequest = new DeleteMetaRequest(20L, "comment");
        HttpResponse      metaResponse  = sendStandardRequest(UrlMapping.FOLDER__DELETE_META, deleteRequest);
        assertCinnamonError(metaResponse, ErrorCode.NO_WRITE_CUSTOM_METADATA_PERMISSION, SC_UNAUTHORIZED);
    }

    @Test
    public void deleteMetaWithMetaNotFound() throws IOException {
        DeleteMetaRequest deleteRequest = new DeleteMetaRequest(21L, "unknown-type");
        HttpResponse      metaResponse  = sendStandardRequest(UrlMapping.FOLDER__DELETE_META, deleteRequest);
        assertCinnamonError(metaResponse, ErrorCode.METASET_NOT_FOUND, SC_NOT_FOUND);
    }

    @Test
    public void deleteMetaHappyPathById() throws IOException {
        // #7 folder_meta = metaset_type license
        DeleteMetaRequest request  = new DeleteMetaRequest(21L, 7L);
        HttpResponse      response = sendStandardRequest(UrlMapping.FOLDER__DELETE_META, request);
        assertResponseOkay(response);
        assertTrue(parseGenericResponse(response).isSuccessful());
    }

    @Test
    public void deleteMetaHappyPathByName() throws IOException {
        // #5 + #6 folder_meta = metaset_type comment
        DeleteMetaRequest request  = new DeleteMetaRequest(21L, "comment");
        HttpResponse      response = sendStandardRequest(UrlMapping.FOLDER__DELETE_META, request);
        assertResponseOkay(response);
        assertTrue(parseGenericResponse(response).isSuccessful());
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
