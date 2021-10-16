package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.client.CinnamonClientException;
import com.dewarim.cinnamon.model.Format;
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
        List<Format> formats = client.listFormats();

        assertNotNull(formats);
        assertFalse(formats.isEmpty());

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

    @Test
    public void createFormatHappyPath() throws IOException {
        var format = adminClient.createFormat("text/csv", "csv", "csv",1L);
        assertEquals("text/csv", format.getContentType());
        assertEquals("csv", format.getExtension());
        assertEquals("csv", format.getName());
        assertEquals(1L, format.getDefaultObjectTypeId());
        assertNotNull(format.getId());
    }

    @Test
    public void createFormatWithoutSuperuserStatus() {
        var ex = assertThrows(CinnamonClientException.class, () -> client.createFormat("text/csv", "csv", "csv-2",1L));
        assertEquals(ErrorCode.REQUIRES_SUPERUSER_STATUS, ex.getErrorCode());
    }

    @Test
    public void createFormatWhichExists() throws IOException {
        var format = adminClient.createFormat("text/dublette", "dub", "dub",1L);
        var ex = assertThrows(CinnamonClientException.class, () -> adminClient.createFormat("text/dublette", "dub", "dub",1L));
        assertEquals(ErrorCode.DB_INSERT_FAILED, ex.getErrorCode());
    }

    @Test
    public void createFormatInvalidRequest() {
        var ex = assertThrows(CinnamonClientException.class, () -> adminClient.createFormat(null, "csv", "csv-2",1L));
        assertEquals(ErrorCode.INVALID_REQUEST, ex.getErrorCode());
    }

    @Test
    public void updateFormatHappyPath() throws IOException{
        var format = adminClient.createFormat("text/update", "up", "update",1L);
        format.setName("updated");
        adminClient.updateFormat(format);
    }

    @Test
    public void deleteFormatHappyPath() throws IOException{
        var format = adminClient.createFormat("text/delete", "del", "delete",1L);
        adminClient.deleteFormat(format.getId());
    }

    @Test
    public void deleteFormatUnknownId() {
        CinnamonClientException ex = assertThrows(CinnamonClientException.class, () -> adminClient.deleteFormat(Long.MAX_VALUE));
        assertEquals(ErrorCode.OBJECT_NOT_FOUND, ex.getErrorCode());
    }

}
