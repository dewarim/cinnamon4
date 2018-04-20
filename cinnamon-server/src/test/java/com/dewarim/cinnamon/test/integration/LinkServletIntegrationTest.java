package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.application.ErrorCode;
import com.dewarim.cinnamon.application.UrlMapping;
import com.dewarim.cinnamon.model.Folder;
import com.dewarim.cinnamon.model.LinkType;
import com.dewarim.cinnamon.model.ObjectSystemData;
import com.dewarim.cinnamon.model.request.DeleteByIdRequest;
import com.dewarim.cinnamon.model.request.LinkRequest;
import com.dewarim.cinnamon.model.response.DeletionResponse;
import com.dewarim.cinnamon.model.response.LinkResponse;
import com.dewarim.cinnamon.model.response.LinkWrapper;
import org.apache.http.HttpResponse;
import org.junit.Test;

import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


public class LinkServletIntegrationTest extends CinnamonIntegrationTest{
    
    @Test
    public void getLinkByIdForObject() throws IOException {
        String adminTicket = ticket;
        try{
            ticket = getDoesTicket(false);
            // request the first link, which points to the first test object with default acl:
            LinkRequest linkRequest = new LinkRequest(1L, true);
            HttpResponse response = sendRequest(UrlMapping.LINK__GET_LINK_BY_ID, linkRequest);
            LinkWrapper linkWrapper = parseResponse(response);
            assertThat(linkWrapper.getLinks().size(), equalTo(1));
            LinkResponse link = linkWrapper.getLinks().get(0);
            assertThat(link.getFolder(),nullValue());
            assertThat(link.getLinkType(), equalTo(LinkType.OBJECT));
            ObjectSystemData osd = link.getOsd();
            assertNotNull(osd);
            assertThat(osd.getSummary(), equalTo("<summary>sum of sum</summary>"));
        }
        finally {
            // restore admin ticket for later tests.
            ticket = adminTicket;
        }
    }        
    
    @Test
    public void getLinkByIdForObjectWithoutSummary() throws IOException {
        String adminTicket = ticket;
        try{
            ticket = getDoesTicket(false);
            // request the first link, which points to the first test object with default acl:
            LinkRequest linkRequest = new LinkRequest(1L, false);
            HttpResponse response = sendRequest(UrlMapping.LINK__GET_LINK_BY_ID, linkRequest);
            LinkWrapper linkWrapper = parseResponse(response);
            LinkResponse link = linkWrapper.getLinks().get(0);
            ObjectSystemData osd = link.getOsd();
            assertThat(osd.getSummary(), nullValue());
        }
        finally {
            // restore admin ticket for later tests.
            ticket = adminTicket;
        }
    }    
    
    @Test
    public void getLinkByIdForFolder() throws IOException {
        String adminTicket = ticket;
        try{
            ticket = getDoesTicket(false);
            // request link #2, which points to the "home" folder with default acl:
            LinkRequest linkRequest = new LinkRequest(2L, true);
            HttpResponse response = sendRequest(UrlMapping.LINK__GET_LINK_BY_ID, linkRequest);
            LinkWrapper linkWrapper = parseResponse(response);
            assertThat(linkWrapper.getLinks().size(), equalTo(1));
            LinkResponse link = linkWrapper.getLinks().get(0);
            assertThat(link.getOsd(),nullValue());
            assertThat(link.getLinkType(), equalTo(LinkType.FOLDER));
            Folder folder = link.getFolder();
            assertNotNull(folder);
            assertThat(folder.getName(), equalTo("home"));
            assertThat(folder.getSummary(), equalTo("<summary>stuff</summary>"));

        }
        finally {
            // restore admin ticket for later tests.
            ticket = adminTicket;
        }
    }        
    
    @Test
    public void getLinkByIdForFolderWithoutSummary() throws IOException {
        String adminTicket = ticket;
        try{
            ticket = getDoesTicket(false);
            // request link #2, which points to the "home" folder with default acl:
            LinkRequest linkRequest = new LinkRequest(2L, false);
            HttpResponse response = sendRequest(UrlMapping.LINK__GET_LINK_BY_ID, linkRequest);
            LinkWrapper linkWrapper = parseResponse(response);
            LinkResponse link = linkWrapper.getLinks().get(0);
            assertThat(link.getLinkType(), equalTo(LinkType.FOLDER));
            Folder folder = link.getFolder();
            assertThat(folder.getSummary(), nullValue());
        }
        finally {
            // restore admin ticket for later tests.
            ticket = adminTicket;
        }
    }    

    @Test
    public void aclOnLinkForbidsAccess() throws IOException{
        String adminTicket = ticket;
        try{
            ticket = getDoesTicket(false);
            // request link #2, which points to the "home" folder with default acl:
            LinkRequest linkRequest = new LinkRequest(3L, false);
            HttpResponse response = sendRequest(UrlMapping.LINK__GET_LINK_BY_ID, linkRequest);
            assertCinnamonError(response, ErrorCode.UNAUTHORIZED, SC_UNAUTHORIZED);
        }
        finally {
            // restore admin ticket for later tests.
            ticket = adminTicket; 
        }
    }
    
    @Test
    public void aclOnObjectForbidsAccess() throws IOException{
        String adminTicket = ticket;
        try{
            ticket = getDoesTicket(false);
            // request link #4, which points to object #7 with acl "unseen" #7:
            LinkRequest linkRequest = new LinkRequest(4L, false);
            HttpResponse response = sendRequest(UrlMapping.LINK__GET_LINK_BY_ID, linkRequest);
            assertCinnamonError(response, ErrorCode.UNAUTHORIZED, SC_UNAUTHORIZED);
        }
        finally {
            // restore admin ticket for later tests.
            ticket = adminTicket; 
        }
    }
    
    @Test
    public void aclOnFolderForbidsAccess() throws IOException{
        String adminTicket = ticket;
        try{
            ticket = getDoesTicket(false);
            // request link #5, which points to folder #3 with acl "unseen" #7:
            LinkRequest linkRequest = new LinkRequest(5L, false);
            HttpResponse response = sendRequest(UrlMapping.LINK__GET_LINK_BY_ID, linkRequest);
            assertCinnamonError(response, ErrorCode.UNAUTHORIZED, SC_UNAUTHORIZED);
        }
        finally {
            // restore admin ticket for later tests.
            ticket = adminTicket; 
        }
    }

    @Test
    public void getLinkByIdForLatestHead() throws IOException {
        String adminTicket = ticket;
        try{
            ticket = getDoesTicket(false);
            // request link #6, which points to osd#8, but should return latest head osd#9:
            LinkRequest linkRequest = new LinkRequest(6L, false);
            HttpResponse response = sendRequest(UrlMapping.LINK__GET_LINK_BY_ID, linkRequest);
            LinkWrapper linkWrapper = parseResponse(response);
            LinkResponse link = linkWrapper.getLinks().get(0);
            ObjectSystemData osd = link.getOsd();
            assertThat(osd.getId(), equalTo(9L));
            assertThat(osd.getName(), equalTo("test-child"));
            
        }
        finally {
            // restore admin ticket for later tests.
            ticket = adminTicket;
        }
    }


    @Test
    public void deleteLinkWithInvalidId() throws IOException {
        String adminTicket = ticket;
        try{
            ticket = getDoesTicket(false);
            DeleteByIdRequest deleteRequest = new DeleteByIdRequest(-1L);
            HttpResponse response = sendRequest(UrlMapping.LINK__DELETE_LINK, deleteRequest);
            assertCinnamonError(response, ErrorCode.ID_PARAM_IS_INVALID, SC_BAD_REQUEST);
        }
        finally {
            // restore admin ticket for later tests.
            ticket = adminTicket;
        }  
    }
    
    @Test
    public void deleteLinkWithNonExistantLink() throws IOException {
        String adminTicket = ticket;
        try{
            ticket = getDoesTicket(false);
            DeleteByIdRequest deleteRequest = new DeleteByIdRequest(Long.MAX_VALUE);
            HttpResponse response = sendRequest(UrlMapping.LINK__DELETE_LINK, deleteRequest);
            assertResponseOkay(response);
            DeletionResponse deletionResponse = mapper.readValue(response.getEntity().getContent(), DeletionResponse.class);
            assertTrue(deletionResponse.isNotFound());
            assertFalse(deletionResponse.isSuccess());
        }
        finally {
            // restore admin ticket for later tests.
            ticket = adminTicket;
        }  
    }
    
    @Test
    public void deleteLinkWithMissingBrowsePermission() throws IOException {
        String adminTicket = ticket;
        try{
            ticket = getDoesTicket(false);
            DeleteByIdRequest deleteRequest = new DeleteByIdRequest(11L);
            HttpResponse response = sendRequest(UrlMapping.LINK__DELETE_LINK, deleteRequest);
            assertCinnamonError(response, ErrorCode.UNAUTHORIZED, SC_UNAUTHORIZED);
        }
        finally {
            // restore admin ticket for later tests.
            ticket = adminTicket;
        }
    }

    @Test
    public void deleteLinkToObjectWithoutPermission() throws IOException {
        String adminTicket = ticket;
        try{
            ticket = getDoesTicket(false);
            DeleteByIdRequest deleteRequest = new DeleteByIdRequest(12L);
            HttpResponse response = sendRequest(UrlMapping.LINK__DELETE_LINK, deleteRequest);
            assertCinnamonError(response, ErrorCode.UNAUTHORIZED, SC_UNAUTHORIZED);
        }
        finally {
            // restore admin ticket for later tests.
            ticket = adminTicket;
        }
    }
    
    @Test
    public void deleteLinkToFolderWithoutPermission() throws IOException {
        String adminTicket = ticket;
        try{
            ticket = getDoesTicket(false);
            DeleteByIdRequest deleteRequest = new DeleteByIdRequest(13L);
            HttpResponse response = sendRequest(UrlMapping.LINK__DELETE_LINK, deleteRequest);
            assertCinnamonError(response, ErrorCode.UNAUTHORIZED, SC_UNAUTHORIZED);
        }
        finally {
            // restore admin ticket for later tests.
            ticket = adminTicket;
        }
    }

    @Test
    public void deleteObjectLinkHappyPath() throws IOException {
        String adminTicket = ticket;
        try{
            ticket = getDoesTicket(false);
            DeleteByIdRequest deleteRequest = new DeleteByIdRequest(9L);
            HttpResponse response = sendRequest(UrlMapping.LINK__DELETE_LINK, deleteRequest);
            assertResponseOkay(response);
            DeletionResponse deletionResponse = mapper.readValue(response.getEntity().getContent(), DeletionResponse.class);
            assertFalse(deletionResponse.isNotFound());
            assertTrue(deletionResponse.isSuccess());
         
            // verify delete:
            LinkRequest linkRequest = new LinkRequest(9L, true);
            HttpResponse linkResponse = sendRequest(UrlMapping.LINK__GET_LINK_BY_ID, linkRequest);
            assertCinnamonError(linkResponse, ErrorCode.OBJECT_NOT_FOUND, SC_NOT_FOUND);
        }
        finally {
            // restore admin ticket for later tests.
            ticket = adminTicket;
        }
    }

    @Test
    public void deleteFolderLinkHappyPath() throws IOException {
        String adminTicket = ticket;
        try{
            ticket = getDoesTicket(false);
            DeleteByIdRequest deleteRequest = new DeleteByIdRequest(10L);
            HttpResponse response = sendRequest(UrlMapping.LINK__DELETE_LINK, deleteRequest);
            assertResponseOkay(response);
            DeletionResponse deletionResponse = mapper.readValue(response.getEntity().getContent(), DeletionResponse.class);
            assertFalse(deletionResponse.isNotFound());
            assertTrue(deletionResponse.isSuccess());

            // verify delete:
            LinkRequest linkRequest = new LinkRequest(10L, true);
            HttpResponse linkResponse = sendRequest(UrlMapping.LINK__GET_LINK_BY_ID, linkRequest);
            assertCinnamonError(linkResponse, ErrorCode.OBJECT_NOT_FOUND, SC_NOT_FOUND);
        }
        finally {
            // restore admin ticket for later tests.
            ticket = adminTicket;
        }
    }

    private LinkWrapper parseResponse(HttpResponse response) throws IOException{
        assertResponseOkay(response);
        return mapper.readValue(response.getEntity().getContent(), LinkWrapper.class);
    }
}
