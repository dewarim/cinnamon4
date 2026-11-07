package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.application.CinnamonServer;
import com.dewarim.cinnamon.test.TestObjectHolder;
import org.apache.ibatis.session.SqlSession;
import com.dewarim.cinnamon.dao.IndexEventDao;
import com.dewarim.cinnamon.dao.TikaFailureDao;
import com.dewarim.cinnamon.dao.event.AccessLogDao;
import com.dewarim.cinnamon.model.event.AccessLogEntry;
import com.dewarim.cinnamon.model.index.IndexEvent;
import com.dewarim.cinnamon.model.index.IndexEventType;
import com.dewarim.cinnamon.model.index.IndexResult;
import com.dewarim.cinnamon.model.index.TikaFailure;
import com.dewarim.cinnamon.model.request.admin.ListAccessLogRequest;
import com.dewarim.cinnamon.model.request.admin.ListFailedTikaExtractionsRequest;
import com.dewarim.cinnamon.model.request.admin.ListIndexEventsRequest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The admin endpoints: what the server recorded about its own failures. Both lists were reachable
 * only by SQL before - IndexService wrote index_events and nothing read them, and the access log
 * had no reader at all.
 */
public class AdminServletIntegrationTest extends CinnamonIntegrationTest {

    /**
     * Write index events through the DAO rather than by breaking the index: a genuine Lucene failure
     * is hard to provoke on purpose, and this test is about the read path and its limit.
     */
    private List<Long> writeIndexEvents(IndexEventType eventType, String message, int count) {
        List<IndexEvent> events = IntStream.range(0, count)
                .mapToObj(index -> new IndexEvent(0L, eventType, IndexResult.FAILED, message + " #" + index))
                .toList();
        try (SqlSession sqlSession = CinnamonServer.getSqlSession()) {
            new IndexEventDao(sqlSession).create(events);
            sqlSession.commit();
        }
        return events.stream().map(IndexEvent::getId).toList();
    }

    @Test
    public void listIndexEventsReturnsTheNewestFirst() throws Exception {
        List<Long> ids = writeIndexEvents(IndexEventType.LUCENE, "listIndexEventsReturnsTheNewestFirst", 3);

        List<IndexEvent> events = adminClient.listIndexEvents(null, null);
        assertFalse(events.isEmpty());
        Long newest = ids.getLast();
        assertEquals(newest, events.getFirst().getId(), "the newest event should come first");
        assertTrue(events.getFirst().getMessage().endsWith("#2"));
        assertEquals(IndexEventType.LUCENE, events.getFirst().getEventType());
        // the message and the result are what an operator is after, so they have to survive the trip:
        assertEquals(IndexResult.FAILED, events.getFirst().getIndexResult());
    }

    /**
     * IO on purpose: the server writes LUCENE, GENERIC and ERROR events, so a real indexing failure
     * elsewhere in the suite cannot sneak into these assertions.
     */
    @Test
    public void listIndexEventsHonoursTheLimitAndTheTypeFilter() throws Exception {
        writeIndexEvents(IndexEventType.IO, "listIndexEventsHonoursTheLimit", 3);

        assertEquals(2, adminClient.listIndexEvents(2, null).size());

        List<IndexEvent> ioEvents = adminClient.listIndexEvents(100, IndexEventType.IO);
        assertFalse(ioEvents.isEmpty());
        assertTrue(ioEvents.stream().allMatch(event -> event.getEventType() == IndexEventType.IO),
                "a filtered list must not contain other event types");

        // the type filter is a where clause, not a filter applied to the limited rows:
        writeIndexEvents(IndexEventType.GENERIC, "listIndexEventsHonoursTheLimit-generic", 5);
        List<IndexEvent> stillFound = adminClient.listIndexEvents(2, IndexEventType.IO);
        assertEquals(2, stillFound.size(), "the newest IO events, not the newest events that are IO");
        assertTrue(stillFound.stream().allMatch(event -> event.getEventType() == IndexEventType.IO));
    }

    @Test
    public void indexEventLimitIsValidated() {
        assertClientError(() -> adminClient.listIndexEvents(0, null), ErrorCode.INVALID_REQUEST);
        assertClientError(() -> adminClient.listIndexEvents(ListIndexEventsRequest.MAX_LIMIT + 1, null),
                ErrorCode.INVALID_REQUEST);
    }

    @Test
    public void listIndexEventsRequiresSuperuserStatus() {
        assertClientError(() -> client.listIndexEvents(10, null), ErrorCode.REQUIRES_SUPERUSER_STATUS);
    }

    @Test
    public void listAccessLogReturnsTheNewestFirst() throws Exception {
        AccessLogDao   accessLogDao = new AccessLogDao();
        AccessLogEntry entry        = new AccessLogEntry();
        entry.setUrl("/api/test/listAccessLogReturnsTheNewestFirst");
        entry.setRequest("<request/>");
        entry.setResponse("<response/>");
        entry.setErrorCode(ErrorCode.OBJECT_NOT_FOUND);
        entry.setErrorMessage("a message");
        accessLogDao.insert(entry);

        List<AccessLogEntry> entries = adminClient.listAccessLog(null);
        assertFalse(entries.isEmpty());
        AccessLogEntry newest = entries.getFirst();
        assertEquals(entry.getId(), newest.getId(), "the newest entry should come first");
        assertEquals("/api/test/listAccessLogReturnsTheNewestFirst", newest.getUrl());
        assertEquals(ErrorCode.OBJECT_NOT_FOUND, newest.getErrorCode());
        assertEquals("<request/>", newest.getRequest());
        assertEquals("a message", newest.getErrorMessage());
        assertEquals(1, adminClient.listAccessLog(1).size());
    }

    /**
     * The point of the endpoint: a request the server refused shows up in the log, without anyone
     * having to read the server's log file or the database.
     * <p>
     * It has to be a request the <i>server</i> rejects. Asking for an object that does not exist
     * looks like a failure to a client - the client raises OBJECT_NOT_FOUND when the list it gets
     * back is empty - but the server answered 200 with an empty list and has nothing to log. The
     * refused permission below is a FailedRequestException, which is what RequestResponseFilter
     * writes an entry for.
     */
    @Test
    public void aFailedRequestIsVisibleInTheAccessLog() throws Exception {
        assertClientError(() -> client.listAccessLog(10), ErrorCode.REQUIRES_SUPERUSER_STATUS);

        List<AccessLogEntry> entries = adminClient.listAccessLog(ListAccessLogRequest.MAX_LIMIT);
        assertTrue(entries.stream().anyMatch(entry ->
                        entry.getUrl().endsWith("/api/admin/listAccessLog")
                                && entry.getErrorCode() == ErrorCode.REQUIRES_SUPERUSER_STATUS),
                "the refused request should have been logged");
    }

    /**
     * The other half of "why is this document not searchable". The rows are the same ones index/info
     * returns for listFailedTikaExtractions, which is why this only has to check that the endpoint
     * reaches them, honours its limit and is closed to everyone else.
     */
    @Test
    public void listFailedTikaExtractionsReturnsTheRecordedFailures() throws Exception {
        // a real object: tika_failures.osd_id is a foreign key into objects. Without content it is
        // not part of the extraction queue, so the row below is the only thing this test adds.
        long osdId = new TestObjectHolder(client, userId)
                .createOsd("listFailedTikaExtractions-osd").osd.getId();
        try (SqlSession sqlSession = CinnamonServer.getSqlSession()) {
            TikaFailureDao dao = new TikaFailureDao(sqlSession);
            dao.recordFailure(osdId, "listFailedTikaExtractionsReturnsTheRecordedFailures");
            sqlSession.commit();
        }
        try {
            List<TikaFailure> failures = adminClient.listFailedTikaExtractions(null);
            TikaFailure failure = failures.stream()
                    .filter(row -> osdId == row.getOsdId()).findFirst().orElseThrow();
            assertEquals("listFailedTikaExtractionsReturnsTheRecordedFailures", failure.getLastError());
            assertTrue(failure.getAttempts() >= 1);
            assertNotNull(failure.getLastAttempt(), "the time of the last attempt should be reported");
            assertEquals(1, adminClient.listFailedTikaExtractions(1).size());
        }
        finally {
            // leave the Tika queue as it was: a leftover row stops TikaService from retrying this
            // object for the rest of the suite.
            try (SqlSession sqlSession = CinnamonServer.getSqlSession()) {
                new TikaFailureDao(sqlSession).deleteByOsdIds(List.of(osdId));
                sqlSession.commit();
            }
        }
    }

    @Test
    public void tikaExtractionLimitIsValidated() {
        assertClientError(() -> adminClient.listFailedTikaExtractions(0), ErrorCode.INVALID_REQUEST);
        assertClientError(() -> adminClient.listFailedTikaExtractions(ListFailedTikaExtractionsRequest.MAX_LIMIT + 1),
                ErrorCode.INVALID_REQUEST);
    }

    @Test
    public void listFailedTikaExtractionsRequiresSuperuserStatus() {
        assertClientError(() -> client.listFailedTikaExtractions(10), ErrorCode.REQUIRES_SUPERUSER_STATUS);
    }

    @Test
    public void accessLogLimitIsValidated() {
        assertClientError(() -> adminClient.listAccessLog(0), ErrorCode.INVALID_REQUEST);
        assertClientError(() -> adminClient.listAccessLog(ListAccessLogRequest.MAX_LIMIT + 1),
                ErrorCode.INVALID_REQUEST);
    }

    @Test
    public void listAccessLogRequiresSuperuserStatus() {
        assertClientError(() -> client.listAccessLog(10), ErrorCode.REQUIRES_SUPERUSER_STATUS);
    }

    /**
     * The retention rule: the table is pruned down by truncateIndexEventsBy once it has grown past
     * truncateIndexEventsAfterRow. Driven through the DAO here, because the rule itself is applied by
     * IndexService after it has written events, which this test cannot provoke.
     */
    @Test
    public void indexEventsArePruned() {
        writeIndexEvents(IndexEventType.GENERIC, "indexEventsArePruned", 10);
        try (SqlSession sqlSession = CinnamonServer.getSqlSession()) {
            IndexEventDao dao    = new IndexEventDao(sqlSession);
            int           before = dao.count();
            assertTrue(before >= 10);

            int deleted = dao.truncate(5);
            sqlSession.commit();
            assertEquals(5, deleted);
            assertEquals(before - 5, dao.count());

            // a value of zero or less would delete nothing and is refused, so that a misconfigured
            // truncateIndexEventsBy cannot turn into a statement that runs on every pass and
            // achieves nothing:
            assertEquals(0, dao.truncate(0));
        }
    }

    @Test
    public void indexEventRetentionSettingsHaveUsableDefaults() {
        var luceneConfig = CinnamonServer.getConfig().getLuceneConfig();
        assertTrue(luceneConfig.getTruncateIndexEventsAfterRow() > 0,
                "pruning should be on by default, or index_events grows without bound");
        assertTrue(luceneConfig.getTruncateIndexEventsBy() > 0,
                "pruning has to delete something to make progress");
        assertTrue(luceneConfig.isLogFailedAttemptsToDb(),
                "the events are what the admin endpoint reads, so they are written by default");
    }
}
