package com.dewarim.cinnamon.test.unit;

import com.dewarim.cinnamon.application.CinnamonServer;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * The version lives in two places: pom.xml and {@link CinnamonServer#VERSION}.
 * This fails the build when a release bumps one but not the other.
 */
public class VersionConsistencyTest {

    @Test
    public void serverVersionMatchesPomVersion() throws Exception {
        // surefire runs with the project root as working directory
        Element project = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(new File("pom.xml")).getDocumentElement();
        assertEquals(CinnamonServer.VERSION, pomVersion(project),
                "CinnamonServer.VERSION must match the <version> in pom.xml");
    }

    // only the direct child of <project>, not <parent>/<version> or a dependency's version
    private String pomVersion(Element project) {
        for (Node node = project.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (node instanceof Element element && element.getTagName().equals("version")) {
                return element.getTextContent().trim();
            }
        }
        return fail("pom.xml has no <project>/<version>");
    }
}
