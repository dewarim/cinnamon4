package com.dewarim.cinnamon.application.service.index;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.DateTools;
import org.dom4j.Node;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * <p>The DateTimeIndexer expects an XPath parameter as searchString and will store
 * the results of this search in the Lucene document.</p>
 * <p>Dates must be formatted as YYYY-MM-DDThh:mm:ss.</p>
 */
public class DateTimeIndexer extends DefaultIndexer {

    private static final Logger log = LogManager.getLogger(DateTimeIndexer.class);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public DateTimeIndexer() {
        fieldType.setTokenized(false);
    }

    /**
     * Convert a node containing a date formatted as
     * "2009-10-01T16:10:30" into an indexable string,
     * which is the time in
     */
    public String convertNodeToString(Node node) {
        String val = node.getStringValue();
        log.debug("Trying to index: {}", val);

        String result = null;
        try {
            LocalDateTime localDateTime = LocalDateTime.parse(val, DATE_TIME_FORMATTER);
            long          millis        = localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            result = DateTools.timeToString(millis, DateTools.Resolution.MILLISECOND);
        } catch (Exception e) {
            log.debug("failed to parse date: {}", val, e);
        }
        log.debug("Result of date conversion: {}", result);
        return result;
    }


}
