package com.dewarim.cinnamon.application.service.index;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Node;

/**
 * <p>The DateXpathIndexer expects an XPath parameter as searchString and will stored
 * the results of this search in the Lucene document.</p>
 * <p>Dates must be formatted as YYYY-MM-DDThh:mm:ss.</p>
 */
public class DateIndexer extends DefaultIndexer {

    private static final Logger log = LogManager.getLogger(DateIndexer.class);


    /**
     * Convert a node containing a date formatted as
     * "2009-10-01T16:10:30" into an indexable string,
     * omitting the time of day.
     */
    public String convertNodeToString(Node node) {
        String val = node.getStringValue();
        log.trace("Trying to index: {}", val);

        String result = null;
        try {
            // currently, index with a resolution of DAY
            String[] parts = val.split("T");
            result = parts[0];
            result = result.replace("-", "");
        } catch (Exception e) {
            log.debug("failed to split date:", e);
        }
        log.trace("Result of date conversion: {}", result);
        return result;
    }

}
