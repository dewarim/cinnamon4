package com.dewarim.cinnamon.application.service.index;

import com.dewarim.cinnamon.application.CinnamonServer;
import com.dewarim.cinnamon.configuration.CinnamonConfig;
import com.dewarim.cinnamon.configuration.XmlParserConfig;
import org.dom4j.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.dewarim.cinnamon.api.Constants.XML_MAPPER;
import static org.junit.jupiter.api.Assertions.*;

public class ParamParserUnitTest {

    /**
     * DOCTYPE_OR_ENTITY_PATTERN only strips once, so nesting a declaration inside another one rebuilds
     * a working DTD - the parser limits, not the pattern, have to protect against entity attacks.
     */
    private static final String DTD_START    = "<!DOC<!ENTITY>TYPE r [";
    private static final String ENTITY_START = "<!EN<!ENTITY>TITY ";

    private XmlParserConfig originalXmlParserConfig;

    @BeforeEach
    public void saveConfig() {
        originalXmlParserConfig = CinnamonServer.getConfig().getXmlParserConfig();
    }

    @AfterEach
    public void restoreConfig() {
        CinnamonServer.getConfig().setXmlParserConfig(originalXmlParserConfig);
    }

    @DisplayName("Default limits are those of JDK 25's jaxp.properties")
    @Test
    public void defaultLimits() {
        XmlParserConfig config = new XmlParserConfig();
        assertEquals(2500, config.getEntityExpansionLimit());
        assertEquals(100_000, config.getTotalEntitySizeLimit());
        assertEquals(100_000, config.getMaxGeneralEntitySizeLimit());
        assertEquals(15_000, config.getMaxParameterEntitySizeLimit());
        assertEquals(100_000, config.getEntityReplacementLimit());
        assertEquals(200, config.getElementAttributeLimit());
        assertEquals(100, config.getMaxElementDepth());
        assertEquals(1000, config.getMaxXMLNameLimit());
    }

    @DisplayName("Nested DOCTYPE bypasses the pattern, but external entities are still not resolved")
    @Test
    public void externalEntityIsNotResolved(@TempDir Path tempDir) throws IOException {
        Path secret = Files.writeString(tempDir.resolve("secret.txt"), "secret-content");
        String xml = DTD_START + ENTITY_START + "x SYSTEM \"" + secret.toUri() + "\">]><r>&x;</r>";
        Document doc = ParamParser.parseXmlToDocument(xml);
        assertEquals("", doc.getRootElement().getText());
    }

    @DisplayName("Nested DOCTYPE bypasses the pattern, but entity expansion is limited (billion laughs)")
    @Test
    public void billionLaughsIsRejected() {
        StringBuilder xml = new StringBuilder(DTD_START).append(ENTITY_START).append("a0 \"lol\">");
        for (int i = 1; i <= 9; i++) {
            xml.append(ENTITY_START).append("a").append(i).append(" \"").append(("&a" + (i - 1) + ";").repeat(10)).append("\">");
        }
        xml.append("]><r>&a9;</r>");
        assertThrows(RuntimeException.class, () -> ParamParser.parseXmlToDocument(xml.toString()));
    }

    @DisplayName("Internal entities within the limits are expanded")
    @Test
    public void internalEntityWithinLimits() {
        Document doc = ParamParser.parseXmlToDocument(DTD_START + ENTITY_START + "a \"lol\">]><r>&a;&a;</r>");
        assertEquals("lollol", doc.getRootElement().getText());
    }

    @DisplayName("Configured entityExpansionLimit is applied")
    @Test
    public void lowerEntityExpansionLimit() {
        String xml = DTD_START + ENTITY_START + "a \"lol\">]><r>" + "&a;".repeat(10) + "</r>";
        assertDoesNotThrow(() -> ParamParser.parseXmlToDocument(xml));

        XmlParserConfig config = new XmlParserConfig();
        config.setEntityExpansionLimit(5);
        CinnamonServer.getConfig().setXmlParserConfig(config);
        assertThrows(RuntimeException.class, () -> ParamParser.parseXmlToDocument(xml));
    }

    @DisplayName("Default maxElementDepth of 100 is applied")
    @Test
    public void defaultMaxElementDepth() {
        assertDoesNotThrow(() -> ParamParser.parseXmlToDocument(nested(100)));
        assertThrows(RuntimeException.class, () -> ParamParser.parseXmlToDocument(nested(101)));
    }

    @DisplayName("Configured maxElementDepth overrides the JRE's jaxp.properties")
    @Test
    public void higherMaxElementDepth() {
        XmlParserConfig config = new XmlParserConfig();
        config.setMaxElementDepth(200);
        CinnamonServer.getConfig().setXmlParserConfig(config);
        assertDoesNotThrow(() -> ParamParser.parseXmlToDocument(nested(200)));
        assertThrows(RuntimeException.class, () -> ParamParser.parseXmlToDocument(nested(201)));
    }

    @DisplayName("xmlParserConfig is read from the config file, missing values keep their defaults")
    @Test
    public void readFromConfigXml() {
        CinnamonConfig config = XML_MAPPER.readValue("""
                <cinnamonConfig>
                  <xmlParserConfig>
                    <maxElementDepth>500</maxElementDepth>
                  </xmlParserConfig>
                </cinnamonConfig>
                """, CinnamonConfig.class);
        assertEquals(500, config.getXmlParserConfig().getMaxElementDepth());
        assertEquals(2500, config.getXmlParserConfig().getEntityExpansionLimit());
    }

    private static String nested(int depth) {
        return "<a>".repeat(depth) + "</a>".repeat(depth);
    }
}
