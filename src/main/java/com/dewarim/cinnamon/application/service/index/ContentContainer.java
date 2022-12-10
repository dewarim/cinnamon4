package com.dewarim.cinnamon.application.service.index;

import com.dewarim.cinnamon.application.exception.CinnamonException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.tree.DefaultDocument;
import org.dom4j.tree.DefaultElement;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static com.dewarim.cinnamon.application.service.IndexService.NO_CONTENT;

/**
 * A content container class which will load the content when needed - unless it was either supplied
 * at instantiation.
 */
public class ContentContainer {

    private final static Logger   log            = LogManager.getLogger(ContentContainer.class);
    private final        byte[]   content;
    private              String   contentAsString;
    private              Document contentAsDocument;
    private final        String   sysMeta;

    /**
     * Instantiate a new ContentContainer object and set the content with a byte[] array.
     *
     * @param content a byte array which holds the content
     */
    public ContentContainer(String sysMeta, byte[] content) {
        this.content = content;
        this.sysMeta = sysMeta;
    }

    public Document getCombinedDocument() {
        Document combinedDoc = ParamParser.parseXmlToDocument(sysMeta);
        Element contentNode = new DefaultElement("content");
        contentNode.add(asNode());
        combinedDoc.getRootElement().add(contentNode);
        log.debug("combinedDocument:\n" + combinedDoc.asXML());
        return combinedDoc;
    }

    /**
     * Parse the content as an XML-document. Will always return a document. In case of invalid content, it
     * returns "&lt;empty /&gt;" as a document.
     *
     * @return a dom4j Document which is either a representation of the content as XML, or an empty document which
     * contains only an "empty" element.
     */
    private Element asNode() {
        if (Arrays.equals(NO_CONTENT, content)) {
            contentAsDocument = new DefaultDocument().addElement("empty").getDocument();
        }
        if (contentAsDocument == null) {
            try {
                // TODO: it would be nice if we could parse pure text files (like markdown). may need some format-detection
                contentAsDocument = ParamParser.parseXmlToDocument(asString());
            } catch (Exception e) {
                log.debug("Failed to parse content. Will create <content/> content.");
                contentAsDocument = new DefaultDocument().addElement("empty").getDocument();
            }
        }
        return contentAsDocument.getRootElement();
    }

    /**
     * Return the content as a String. This method always returns a String, which may be empty in case
     * the content is not defined.
     *
     * @return the content bytes converted to a String. Note: this has not been tested with conflicting locales,
     * so if you upload a string data in binary form in a different encoding than the server system locale, this method
     * might not prove 100% reliable.
     * @throws CinnamonException is thrown if the content cannot be loaded.
     */
    public String asString() throws CinnamonException {
        if (contentAsString == null) {
            contentAsString = new String(content, StandardCharsets.UTF_8);
        }
        return contentAsString;
    }


}
