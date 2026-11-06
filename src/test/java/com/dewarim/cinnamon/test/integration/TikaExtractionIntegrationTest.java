package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.application.CinnamonServer;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.application.service.TikaService;
import com.dewarim.cinnamon.configuration.CinnamonTikaConfig;
import com.dewarim.cinnamon.dao.OsdDao;
import com.dewarim.cinnamon.dao.TikaFailureDao;
import com.dewarim.cinnamon.model.Format;
import com.dewarim.cinnamon.model.Meta;
import com.dewarim.cinnamon.model.MetasetType;
import com.dewarim.cinnamon.model.ObjectSystemData;
import com.dewarim.cinnamon.model.index.TikaFailure;
import com.dewarim.cinnamon.provider.ContentProviderService;
import com.dewarim.cinnamon.test.FakeTikaServer;
import com.dewarim.cinnamon.test.TestObjectHolder;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;
import java.util.Optional;

import static com.dewarim.cinnamon.api.Constants.TIKA_METASET_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Drives TikaService against a stubbed Tika server (see FakeTikaServer) to pin down what happens
 * when extraction does not work. The rules being tested:
 * <ul>
 *     <li>a document Tika rejects is retried, counted, and eventually given up on - but never
 *     written off by storing the error as its metadata;</li>
 *     <li>a Tika server that cannot be reached at all costs the document nothing;</li>
 *     <li>a successful extraction clears the document's failure history.</li>
 * </ul>
 * The real service thread is not running during the test run (useTika is false in the test
 * configuration), so nothing competes for the queue.
 */
public class TikaExtractionIntegrationTest extends CinnamonIntegrationTest {

    private static final String TIKA_METADATA = """
            <?xml version="1.0" encoding="UTF-8"?>
            <html xmlns="http://www.w3.org/1999/xhtml">
            <head><meta name="dc:title" content="delicious cinnamon bun"/></head>
            <body/>
            </html>""";

    private final File bun = new File("data/cinnamon-bun.png");

    private TikaService tikaServiceFor(String baseUrl, int maxTikaAttempts) {
        CinnamonTikaConfig config = new CinnamonTikaConfig(baseUrl, true);
        config.setMaxTikaAttempts(maxTikaAttempts);
        // keep the retry policy's backoff out of the test's runtime:
        config.setRetryDelayInSeconds(1);
        config.setMaxRetryDelayInSeconds(1);
        return new TikaService(config, new ContentProviderService());
    }

    /**
     * An OSD as TikaService reads it, with the content fields the content provider needs.
     */
    private ObjectSystemData createOsdWithImageContent(String name) throws IOException {
        Format imagePng = TestObjectHolder.formats.stream()
                .filter(format -> format.getName().equals("image.png")).findFirst().orElseThrow();
        Long osdId = new TestObjectHolder(client, userId)
                .createOsdWithContent(name, imagePng, bun).osd.getId();
        ThreadLocalSqlSession.refreshSession();
        return new OsdDao().getObjectById(osdId).orElseThrow();
    }

    private Optional<TikaFailure> failureOf(Long osdId) {
        ThreadLocalSqlSession.refreshSession();
        return new TikaFailureDao().list().stream()
                .filter(failure -> failure.getOsdId().equals(osdId)).findFirst();
    }

    private List<Meta> tikaMetasetsOf(Long osdId) throws IOException {
        MetasetType tikaType = TestObjectHolder.getMetasetType(TIKA_METASET_NAME);
        return client.getOsdMetas(osdId, List.of(tikaType.getId()));
    }

    private Format formatOf(ObjectSystemData osd) {
        return TestObjectHolder.formats.stream()
                .filter(format -> format.getId().equals(osd.getFormatId())).findFirst().orElseThrow();
    }

    @Test
    public void rejectedDocumentIsRetriedAndEventuallyExtracted() throws IOException {
        ObjectSystemData osd   = createOsdWithImageContent("tika-rejected-then-extracted");
        Long             osdId = osd.getId();
        try (FakeTikaServer fakeTika = new FakeTikaServer()) {
            fakeTika.enqueueError(503).enqueueError(422).enqueueMetadata(TIKA_METADATA);
            TikaService tikaService = tikaServiceFor(fakeTika.getBaseUrl(), 3);

            // first pass: Tika is reachable but unhappy.
            tikaService.extractMetaset(osd, formatOf(osd));
            assertTrue(tikaMetasetsOf(osdId).isEmpty(),
                    "an error must not be stored as the document's tika metadata");
            TikaFailure afterFirst = failureOf(osdId).orElseThrow(
                    () -> new AssertionError("the rejection should have been recorded"));
            assertEquals(1, afterFirst.getAttempts());
            assertTrue(afterFirst.getLastError().contains("503"), afterFirst.getLastError());

            // second pass: still unhappy, and the same row counts up.
            tikaService.extractMetaset(osd, formatOf(osd));
            TikaFailure afterSecond = failureOf(osdId).orElseThrow();
            assertEquals(2, afterSecond.getAttempts(), "one row per document, counting up");
            assertTrue(afterSecond.getLastError().contains("422"), afterSecond.getLastError());

            // third pass: Tika answers with metadata.
            tikaService.extractMetaset(osd, formatOf(osd));
            List<Meta> metasets = tikaMetasetsOf(osdId);
            assertEquals(1, metasets.size(), "the extracted metadata should be stored now");
            String content = metasets.getFirst().getContent();
            assertTrue(content.contains("delicious cinnamon bun"), content);
            assertFalse(content.contains("xmlns=\"http://www.w3.org/1999/xhtml\""),
                    "the xhtml namespace is stripped before the metaset is stored");
            assertTrue(failureOf(osdId).isEmpty(),
                    "a successful extraction should clear the document's failure history");

            assertEquals(3, fakeTika.getRequestCount());
            assertTrue(fakeTika.getReceivedContentTypes().getFirst().startsWith("image/png"),
                    "the format's content type is what tells Tika how to parse the upload: "
                            + fakeTika.getReceivedContentTypes());
        }
        finally {
            cleanUp(osdId);
        }
    }

    /**
     * The queue is "content-bearing objects of a TIKA format without a tika metaset", minus the ones
     * given up on. Without that subtraction, a document Tika can never handle would be retried on
     * every pass forever.
     */
    @Test
    public void documentIsDroppedFromTheQueueAfterMaxAttempts() throws IOException {
        ObjectSystemData osd   = createOsdWithImageContent("tika-gives-up");
        Long             osdId = osd.getId();
        Long             tikaTypeId = TestObjectHolder.getMetasetType(TIKA_METASET_NAME).getId();
        try (FakeTikaServer fakeTika = new FakeTikaServer()) {
            fakeTika.enqueueError(500).enqueueError(500);
            TikaService tikaService = tikaServiceFor(fakeTika.getBaseUrl(), 2);

            assertTrue(isInQueue(osdId, tikaTypeId, 2), "a fresh document should be in the queue");
            tikaService.extractMetaset(osd, formatOf(osd));
            assertTrue(isInQueue(osdId, tikaTypeId, 2), "one failure of two is not the end");
            tikaService.extractMetaset(osd, formatOf(osd));

            assertEquals(2, failureOf(osdId).orElseThrow().getAttempts());
            assertFalse(isInQueue(osdId, tikaTypeId, 2),
                    "after maxTikaAttempts the document should no longer be offered");
            // the same rows, read with a higher limit: it is the limit that excludes it, not the
            // document having silently gained a metaset.
            assertTrue(isInQueue(osdId, tikaTypeId, 3),
                    "raising maxTikaAttempts should bring it back - as a reindex with "
                            + "updateTikaMetaset does by deleting the record");
            assertTrue(tikaMetasetsOf(osdId).isEmpty());
        }
        finally {
            cleanUp(osdId);
        }
    }

    /**
     * The failure mode that started this: a Tika server that is down must not cost the documents
     * anything, or an outage would write off everything that was pending while it lasted.
     */
    @Test
    public void unreachableTikaIsNotCountedAgainstTheDocument() throws IOException {
        ObjectSystemData osd        = createOsdWithImageContent("tika-unreachable");
        Long             osdId      = osd.getId();
        Long             tikaTypeId = TestObjectHolder.getMetasetType(TIKA_METASET_NAME).getId();
        int              closedPort;
        try (ServerSocket socket = new ServerSocket(0)) {
            closedPort = socket.getLocalPort();
        }
        try {
            TikaService tikaService = tikaServiceFor("http://127.0.0.1:" + closedPort, 2);
            tikaService.extractMetaset(osd, formatOf(osd));

            assertTrue(failureOf(osdId).isEmpty(),
                    "a connection failure says nothing about the document and must not be counted");
            assertTrue(isInQueue(osdId, tikaTypeId, 2), "the document should still be waiting");
            assertTrue(tikaMetasetsOf(osdId).isEmpty());
        }
        finally {
            cleanUp(osdId);
        }
    }

    /**
     * Content that cannot be read is specific to the document, so it does count - this is the one
     * case that used to log an error on every single pass of the service, forever.
     */
    @Test
    public void unreadableContentIsCountedWithoutAskingTika() throws IOException {
        ObjectSystemData osd   = createOsdWithImageContent("tika-content-gone");
        Long             osdId = osd.getId();
        File contentFile = new File(CinnamonServer.config.getServerConfig().getDataRoot(), osd.getContentPath());
        assertTrue(contentFile.exists());
        assertTrue(contentFile.delete(), "could not remove the content file for the test");
        try (FakeTikaServer fakeTika = new FakeTikaServer()) {
            tikaServiceFor(fakeTika.getBaseUrl(), 3).extractMetaset(osd, formatOf(osd));

            TikaFailure failure = failureOf(osdId).orElseThrow(
                    () -> new AssertionError("a missing content file should be recorded"));
            assertEquals(1, failure.getAttempts());
            assertTrue(failure.getLastError().contains("content is unavailable"), failure.getLastError());
            assertEquals(0, fakeTika.getRequestCount(), "Tika should not have been bothered at all");
        }
        finally {
            cleanUp(osdId);
        }
    }

    private boolean isInQueue(Long osdId, Long tikaMetasetTypeId, int maxAttempts) {
        ThreadLocalSqlSession.refreshSession();
        return new OsdDao().getOsdsMissingTikaMetaset(tikaMetasetTypeId, 1000, maxAttempts)
                .stream().anyMatch(osd -> osd.getId().equals(osdId));
    }

    /**
     * Leave no failure records behind: the index info endpoint reports them globally, and another
     * test's expectations should not depend on this one's leftovers.
     */
    private void cleanUp(Long osdId) {
        ThreadLocalSqlSession.refreshSession();
        new TikaFailureDao().deleteByOsdIds(List.of(osdId));
        ThreadLocalSqlSession.getSqlSession().commit();
    }
}
