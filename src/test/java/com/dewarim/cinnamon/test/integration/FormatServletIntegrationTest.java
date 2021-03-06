package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.model.Format;
import com.dewarim.cinnamon.model.request.format.ListFormatRequest;
import com.dewarim.cinnamon.model.response.FormatWrapper;
import org.apache.http.HttpResponse;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class FormatServletIntegrationTest extends CinnamonIntegrationTest{
    
    @Test
    public void listFormats() throws IOException {
        HttpResponse response = sendStandardRequest(UrlMapping.FORMAT__LIST, new ListFormatRequest());
        List<Format> formats = parseResponse(response);
        
        assertNotNull(formats);
        assertFalse(formats.isEmpty());
        assertThat(formats.size(), equalTo(2));
        
        Optional<Format> xmlOpt = formats.stream().filter(format -> format.getName().equals("xml")).findFirst();
        assertTrue(xmlOpt.isPresent());
        Format xml = xmlOpt.get();
        assertThat(xml.getContentType(), equalTo("application/xml"));
        assertThat(xml.getExtension(), equalTo("xml"));
        assertThat(xml.getDefaultObjectTypeId(), equalTo(1L));
    }
    
    private List<Format> parseResponse(HttpResponse response) throws IOException{
        assertResponseOkay(response);
        FormatWrapper formatWrapper = mapper.readValue(response.getEntity().getContent(), FormatWrapper.class);
        assertNotNull(formatWrapper);
        return formatWrapper.getFormats();
    }

    // TODO: add CRUD tests
    
}
