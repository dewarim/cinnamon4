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

public class OsdServletIntegrationTest extends CinnamonIntegrationTest{
    
    @Test
    public void getObjectsById() throws IOException{
        OsdRequest osdRequest = new OsdRequest();
        osdRequest.setIds(List.of(1L,2L,3L));
        HttpResponse response = sendRequest(UrlMapping.OSD__GET_OBJECTS_BY_ID,osdRequest);
        unwrapOsds(response,3);
    }

    private List<ObjectSystemData> unwrapOsds(HttpResponse response, Integer expectedSize) throws IOException {
        assertResponseOkay(response);
        List<ObjectSystemData> osds = mapper.readValue(response.getEntity().getContent(),OsdWrapper.class).getOsds();
        if (expectedSize != null) {
            assertNotNull(osds);
            assertFalse(osds.isEmpty());
            MatcherAssert.assertThat(osds.size(), equalTo(expectedSize));
        }
        return osds;
    }
    
}
