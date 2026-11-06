package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.dao.event.AccessLogDao;
import com.dewarim.cinnamon.model.event.AccessLogEntry;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The access log is written by RequestResponseFilter and read by nothing in the server, so its read
 * path had never been executed: access_log.created was a timestamptz, which the driver refuses to
 * hand to a LocalDateTime property, and the entry class had no constructor MyBatis could use. Both
 * only show up once a statement actually returns a row, which is what this test is for.
 */
public class AccessLogIntegrationTest extends CinnamonIntegrationTest {

    @Test
    public void accessLogEntriesCanBeReadBack() {
        AccessLogDao   accessLogDao = new AccessLogDao();
        AccessLogEntry entry        = new AccessLogEntry();
        String         url          = "/api/test/accessLogEntriesCanBeReadBack";
        entry.setUrl(url);
        entry.setRequest("<request/>");
        entry.setResponse("<response/>");
        entry.setErrorCode(ErrorCode.INVALID_REQUEST);
        entry.setErrorMessage("a message");
        accessLogDao.insert(entry);
        assertNotNull(entry.getId(), "the insert should have assigned an id");

        List<AccessLogEntry> entries = accessLogDao.list();
        AccessLogEntry       stored  = entries.stream()
                .filter(logEntry -> url.equals(logEntry.getUrl())).findFirst().orElseThrow();
        assertEquals(entry.getId(), stored.getId());
        assertEquals(ErrorCode.INVALID_REQUEST, stored.getErrorCode());
        assertEquals("a message", stored.getErrorMessage());
        // written by the column default rather than by the application:
        assertNotNull(stored.getCreated(), "created should have been read from the database");
        assertTrue(accessLogDao.count() >= entries.size());
    }
}
