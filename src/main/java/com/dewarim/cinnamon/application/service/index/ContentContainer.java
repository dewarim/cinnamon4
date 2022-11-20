package com.dewarim.cinnamon.application.service.index;

import com.dewarim.cinnamon.application.exception.CinnamonException;
import com.dewarim.cinnamon.model.Meta;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.dom.DOMDocument;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static com.dewarim.cinnamon.application.service.IndexService.NO_CONTENT;

/**
 * A content container class which will load the content when needed - unless it was either supplied
 * at instantiation.
 */
// copied from Cinnamon 3, TODO: refactoring
public class ContentContainer {

    private final static Logger log = LogManager.getLogger(ContentContainer.class);

    private final byte[]   content;
    private       String   contentAsString;
    private       Document contentAsDoc;

    /**
     * Instantiate a new ContentContainer object and set the content with a byte[] array.
     *
     * @param content a byte array which holds the content
     */
    public ContentContainer(String sysMeta, byte[] content, List<Meta> metas) {
        this.content = content;
    }

    /**
     * Parse the content as an XML-document. Will always return a document. In case of invalid content, it
     * returns "&lt;empty /&gt;" as a document.
     *
     * @return a dom4j Document which is either a representation of the content as XML, or an empty document which
     * contains only an "empty" element.
     */
    public Document asDocument() {
        if (Arrays.equals(NO_CONTENT, content)) {
            contentAsDoc = new DOMDocument();
        }
        if (contentAsDoc == null) {
            try {
                contentAsDoc = ParamParser.parseXmlToDocument(asString());
                // "error.parse.indexable_object"); // error message is not needed as the exception is not
                // propagated to the end user.
            } catch (Exception e) {
                log.debug("Failed to parse content. Will create <empty/> content.");
                contentAsDoc = ParamParser.parseXmlToDocument("<empty />");
            }
        }
        return contentAsDoc;
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
            contentAsString = new String(asBytes(), StandardCharsets.UTF_8);
        }
        return contentAsString;
    }

    public byte[] asBytes() {
        return content;
    }

}
