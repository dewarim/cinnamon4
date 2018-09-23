package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.application.ErrorCode;
import com.dewarim.cinnamon.application.UrlMapping;
import com.dewarim.cinnamon.model.Folder;
import com.dewarim.cinnamon.model.request.*;
import com.dewarim.cinnamon.model.response.FolderWrapper;
import com.dewarim.cinnamon.model.response.SummaryWrapper;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
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
