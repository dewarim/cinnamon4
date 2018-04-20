package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.application.UrlMapping;
import com.dewarim.cinnamon.model.ObjectSystemData;
import com.dewarim.cinnamon.model.request.OsdRequest;
import com.dewarim.cinnamon.model.response.OsdWrapper;
import org.apache.http.HttpResponse;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class OsdServletIntegrationTest extends CinnamonIntegrationTest {

    @Test
    public void getObjectsById() throws IOException {
        String adminTicket = ticket;
        try {
            ticket = CinnamonIntegrationTest.getDoesTicket(false);
            OsdRequest osdRequest = new OsdRequest();
            osdRequest.setIds(List.of(1L, 2L, 3L, 4L, 5L, 6L));
            HttpResponse response = sendRequest(UrlMapping.OSD__GET_OBJECTS_BY_ID, osdRequest);
            List<ObjectSystemData> dataList = unwrapOsds(response, 5);
            assertFalse(dataList.stream().anyMatch(osd -> osd.getName().equals("unbrowsable-test")));
            
            // test for dynamic groups:
            assertTrue(dataList.stream().anyMatch(osd -> osd.getName().equals("owned-by-doe")));
            assertTrue(dataList.stream().anyMatch(osd -> osd.getName().equals("acl-for-everyone")));
            
        } finally {
            // restore admin ticket for later tests.
            ticket = adminTicket;
        }

    }
    
    @Test
    public void getObjectsByIdForAdmin() throws IOException {
            OsdRequest osdRequest = new OsdRequest();
            osdRequest.setIds(List.of(1L, 2L, 3L, 4L, 5L, 6L));
            HttpResponse response = sendRequest(UrlMapping.OSD__GET_OBJECTS_BY_ID, osdRequest);
            List<ObjectSystemData> dataList = unwrapOsds(response, 6);
            // admin is exempt from permission checks, should get everything:
            assertTrue(dataList.stream().anyMatch(osd -> osd.getName().equals("unbrowsable-test")));
           
            // test for dynamic groups:
            assertTrue(dataList.stream().anyMatch(osd -> osd.getName().equals("owned-by-doe")));
            assertTrue(dataList.stream().anyMatch(osd -> osd.getName().equals("acl-for-everyone")));
    }   
    
    @Test
    public void getObjectsByIdWithoutSummary() throws IOException {
            OsdRequest osdRequest = new OsdRequest();
            osdRequest.setIds(List.of(1L));
            osdRequest.setIncludeSummary(false);
            HttpResponse response = sendRequest(UrlMapping.OSD__GET_OBJECTS_BY_ID, osdRequest);
            List<ObjectSystemData> dataList = unwrapOsds(response, 1);
            assertTrue(dataList.stream().anyMatch(osd -> osd.getSummary() == null));
    }    
    @Test
    public void getObjectsByIdIncludingSummary() throws IOException {
            OsdRequest osdRequest = new OsdRequest();
            osdRequest.setIds(List.of(1L));
            osdRequest.setIncludeSummary(true);
            HttpResponse response = sendRequest(UrlMapping.OSD__GET_OBJECTS_BY_ID, osdRequest);
            List<ObjectSystemData> dataList = unwrapOsds(response, 1);
            assertTrue(dataList.stream().anyMatch(osd -> osd.getSummary().equals("<summary>sum of sum</summary>")));
    }

    private List<ObjectSystemData> unwrapOsds(HttpResponse response, Integer expectedSize) throws IOException {
        assertResponseOkay(response);
        List<ObjectSystemData> osds = mapper.readValue(response.getEntity().getContent(), OsdWrapper.class).getOsds();
        if (expectedSize != null) {
            assertNotNull(osds);
            assertFalse(osds.isEmpty());
            MatcherAssert.assertThat(osds.size(), equalTo(expectedSize));
        }
        return osds;
    }

}
