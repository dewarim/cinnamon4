package com.dewarim.cinnamon.test.unit;

import com.dewarim.cinnamon.application.service.index.ContentContainer;
import com.dewarim.cinnamon.model.ObjectSystemData;
import tools.jackson.core.JacksonException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static com.dewarim.cinnamon.api.Constants.XML_MAPPER;
import static com.dewarim.cinnamon.application.service.IndexService.NO_CONTENT;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ContentContainerTest {

    private static final ObjectSystemData osd       = new ObjectSystemData();
    private final String folderPath = "/home/user/test";

    @BeforeAll
    public static void setup() {
        osd.setName("test-osd");
    }

    @Test
    public void osdWithoutContentToDocument() throws JacksonException {
        ContentContainer contentContainer = new ContentContainer(XML_MAPPER.writeValueAsString(osd), NO_CONTENT, folderPath, "OSD#0");
        assertTrue(contentContainer.getCombinedDocument().asXML().contains("<content><empty/></content>"));
    }

    @Test
    public void osdWithContentToDocument() throws JacksonException {
        byte[]           content          = "<xml>test-string</xml>".getBytes(StandardCharsets.UTF_8);
        ContentContainer contentContainer = new ContentContainer(XML_MAPPER.writeValueAsString(osd), content, folderPath, "OSD#0");
        assertTrue(contentContainer.getCombinedDocument().asXML().contains("<content><xml>test-string</xml></content>"));
    }

    @Test
    public void osdWithBinaryContentToDocument() throws JacksonException {
        byte[]           content          = new byte[]{0, 1, 2, 3, 4};
        ContentContainer contentContainer = new ContentContainer(XML_MAPPER.writeValueAsString(osd), content, folderPath, "OSD#0");
        assertTrue(contentContainer.getCombinedDocument().asXML().contains("<content><empty/></content>"));
    }

}
