package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.model.Format;
import com.dewarim.cinnamon.model.IndexMode;
import com.dewarim.cinnamon.test.TestObjectHolder;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static com.dewarim.cinnamon.ErrorCode.*;
import static com.dewarim.cinnamon.model.IndexMode.PLAIN_TEXT;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class FormatServletIntegrationTest extends CinnamonIntegrationTest {

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

    @Test
    public void createFormatHappyPath() throws IOException {
        var format = adminClient.createFormat("text/csv", "csv", "csv", 1L, PLAIN_TEXT);
        assertEquals("text/csv", format.getContentType());
        assertEquals("csv", format.getExtension());
        assertEquals("csv", format.getName());
        assertEquals(1L, format.getDefaultObjectTypeId());
        assertEquals(PLAIN_TEXT, format.getIndexMode());
        assertNotNull(format.getId());
    }

    @Test
    public void createFormatWithoutSuperuserStatus() {
        assertClientError(() ->
                        client.createFormat("text/csv", "csv", "csv-2", 1L, PLAIN_TEXT),
                REQUIRES_SUPERUSER_STATUS);
    }

    @Test
    public void createFormatWhichExists() throws IOException {
        var format = adminClient.createFormat("text/dublette", "dub", "dub", 1L, IndexMode.NONE);
        assertClientError(() ->
                        adminClient.createFormat("text/dublette", "dub", "dub", 1L, IndexMode.NONE),
                DB_INSERT_FAILED);
    }

    @Test
    public void createFormatInvalidRequest() {
        assertClientError(() ->
                        adminClient.createFormat(null, "csv", "csv-2", 1L, PLAIN_TEXT),
                INVALID_REQUEST);
    }

    @Test
    public void updateFormatHappyPath() throws IOException {
        var format = adminClient.createFormat("text/update", "up", "update", 1L, PLAIN_TEXT);
        format.setName("updated");
        adminClient.updateFormat(format);
    }

    @Test
    public void deleteFormatHappyPath() throws IOException {
        var format = adminClient.createFormat("text/delete", "del", "delete", 1L, PLAIN_TEXT);
        adminClient.deleteFormat(format.getId());
    }

    @Test
    public void deleteFormatUnknownId() {
        assertClientError(() -> adminClient.deleteFormat(Long.MAX_VALUE), OBJECT_NOT_FOUND);
    }

    @Test
    public void allowFormatCreationWithoutDefaultObjectType() throws IOException{
        TestObjectHolder toh = new TestObjectHolder(adminClient,adminId);
        var format = adminClient.createFormat("text/allowed", "xxx", toh.createRandomName(), null, PLAIN_TEXT);
        assertNull(format.getDefaultObjectTypeId());
    }
}
