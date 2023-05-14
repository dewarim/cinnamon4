package com.dewarim.cinnamon.application.service.index;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Node;

/**
 * <p>The TimeXpathIndexer expects an XPath parameter as searchString and will stored
 * the results of this search in the Lucene document.</p>
 * <p>Timestamps must be formatted as YYYY-MM-DDThh:mm:ss.</p>
 */
public class TimeIndexer extends DefaultIndexer {

    private static final Logger log = LogManager.getLogger(TimeIndexer.class);

    /**
     * Convert a node containing a date formatted as
     * "2009-10-01T16:10:30" into an indexable string,
     * omitting the date.
     */
    public String convertNodeToString(Node node) {
        String val = node.getStringValue();
        log.debug("Trying to index: " + val);

        String result = null;
        try {
            String[] parts = val.split("T");
            result = parts[1];
            result = result.replace(":", "");
        } catch (Exception e) {
            log.debug("failed to split Timestamp:", e);
        }
        log.debug("Result of Timestamp conversion: " + result);
        return result;
    }

}
