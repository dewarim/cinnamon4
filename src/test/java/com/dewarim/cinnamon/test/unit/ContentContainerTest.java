package com.dewarim.cinnamon.test.unit;

import com.dewarim.cinnamon.application.service.index.ContentContainer;
import com.dewarim.cinnamon.model.ObjectSystemData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static com.dewarim.cinnamon.application.service.IndexService.NO_CONTENT;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ContentContainerTest {

    private static final ObjectSystemData osd       = new ObjectSystemData();
    private static final XmlMapper        xmlMapper = new XmlMapper();

    @BeforeAll
    public static void setup() {
        osd.setName("test-osd");
    }

    @Test
    public void osdWithoutContentToDocument() throws JsonProcessingException {
        ContentContainer contentContainer = new ContentContainer(xmlMapper.writeValueAsString(osd), NO_CONTENT);
        assertTrue(contentContainer.getCombinedDocument().asXML().contains("<content><empty/></content>"));
    }

    @Test
    public void osdWithContentToDocument() throws JsonProcessingException {
        byte[]           content          = "<xml>test-string</xml>".getBytes(StandardCharsets.UTF_8);
        ContentContainer contentContainer = new ContentContainer(xmlMapper.writeValueAsString(osd), content);
        assertTrue(contentContainer.getCombinedDocument().asXML().contains("<content><xml>test-string</xml></content>"));
    }

    @Test
    public void osdWithBinaryContentToDocument() throws JsonProcessingException {
        byte[]           content          = new byte[]{0, 1, 2, 3, 4};
        ContentContainer contentContainer = new ContentContainer(xmlMapper.writeValueAsString(osd), content);
        assertTrue(contentContainer.getCombinedDocument().asXML().contains("<content><empty/></content>"));
    }

}
