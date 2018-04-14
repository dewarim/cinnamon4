package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.application.UrlMapping;
import com.dewarim.cinnamon.model.Folder;
import com.dewarim.cinnamon.model.LinkType;
import com.dewarim.cinnamon.model.ObjectSystemData;
import com.dewarim.cinnamon.model.request.LinkRequest;
import com.dewarim.cinnamon.model.response.LinkResponse;
import com.dewarim.cinnamon.model.response.LinkWrapper;
import org.apache.http.HttpResponse;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;


public class LinkServletIntegrationTest extends CinnamonIntegrationTest{
    
    @Test
    public void getLinkByIdForObject() throws IOException {
        try{
            ticket = getDoesTicket();
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
            ticket = getAdminTicket();
        }
    }    
    @Test
    public void getLinkByIdForFolder() throws IOException {
        try{
            ticket = getDoesTicket();
            // request link #2, which points to the "home" folder with default acl:
            LinkRequest linkRequest = new LinkRequest(2L, false);
            HttpResponse response = sendRequest(UrlMapping.LINK__GET_LINK_BY_ID, linkRequest);
            LinkWrapper linkWrapper = parseResponse(response);
            assertThat(linkWrapper.getLinks().size(), equalTo(1));
            LinkResponse link = linkWrapper.getLinks().get(0);
            assertThat(link.getOsd(),nullValue());
            assertThat(link.getLinkType(), equalTo(LinkType.FOLDER));
            Folder folder = link.getFolder();
            assertNotNull(folder);
            assertThat(folder.getName(), equalTo("home"));
        }
        finally {

            // restore admin ticket for later tests.
            ticket = getAdminTicket();
        }
    }
    
    // TODO: acl on link forbids access
    // TODO: acl on object foribds access
    // TODO: acl on folder forbids access
    // TODO: resolver latest_head for osd
    // TODO: summary=false should return no summary for OSD
    // TODO: summary=false should return no summary for folder
    
    private LinkWrapper parseResponse(HttpResponse response) throws IOException{
        assertResponseOkay(response);
        return mapper.readValue(response.getEntity().getContent(), LinkWrapper.class);
    }
}
