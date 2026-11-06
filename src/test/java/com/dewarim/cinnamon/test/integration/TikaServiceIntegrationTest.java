package com.dewarim.cinnamon.test.integration;


import com.dewarim.cinnamon.application.exception.TikaFailedException;
import com.dewarim.cinnamon.application.exception.TikaUnavailableException;
import com.dewarim.cinnamon.application.service.TikaService;
import com.dewarim.cinnamon.configuration.CinnamonTikaConfig;
import com.dewarim.cinnamon.model.Format;
import com.dewarim.cinnamon.model.IndexMode;
import com.dewarim.cinnamon.provider.ContentProviderService;
import com.sun.net.httpserver.HttpServer;
import dev.failsafe.FailsafeException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TikaServiceIntegrationTest {
    private static final Logger log = LogManager.getLogger(TikaServiceIntegrationTest.class);

    private final String                 baseUrl                = "http://localhost:9998";
    // cinnamon-bun.png has exif metadata from stable diffusion containing the prompt "delicious cinnamon bun"
    private final File                   bun                    = new File("data/cinnamon-bun.png");
    private final Format                 imageFormat            = new Format("image/png", "png", "image-png", 1L, IndexMode.TIKA);
    private final long                   osdId                  = 0L;
    private final ContentProviderService contentProviderService = new ContentProviderService();

    @Disabled("Requires Apache Tika server @ localhost:9998")
    @Test
    public void tikaHappyTest() throws IOException {
        CinnamonTikaConfig cinnamonTikaConfig = new CinnamonTikaConfig(baseUrl, true);
        String             bunData;
        try (FileInputStream bunnyStream = new FileInputStream(bun)) {
            bunData = new TikaService(cinnamonTikaConfig, contentProviderService)
                    .parseData(bunnyStream, bun.length(), imageFormat, osdId);
            log.info("bunData:\n{}", bunData);
        }
        try {
            Thread.sleep(cinnamonTikaConfig.getTikaPauseInMillis() + 3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        assertTrue(bunData.contains("delicious cinnamon bun"));
        assertTrue(bunData.startsWith("<?xml version=\"1.0\""));
    }

    @Disabled("Requires Apache Tika server @ localhost:9998")
    @Test
    public void tikaVersion11PreservedTest() throws IOException {
        CinnamonTikaConfig cinnamonTikaConfig = new CinnamonTikaConfig(baseUrl, true);
        cinnamonTikaConfig.setRemoveNewXmlVersionHeader(false);
        String             bunData;
        try (FileInputStream bunnyStream = new FileInputStream(bun)) {
            bunData = new TikaService(cinnamonTikaConfig, contentProviderService)
                    .parseData(bunnyStream, bun.length(), imageFormat, osdId);
            log.info("bunData:\n{}", bunData);
        }
        assertTrue(bunData.contains("delicious cinnamon bun"));
        assertTrue(bunData.startsWith("<?xml version=\"1.1\""));
    }

    /**
     * A Tika server that answers with an error must not produce metaset content. A stored
     * tikaFailedToParse document would look like a successful extraction to
     * getOsdsMissingTikaMetaset, and the OSD would never be offered again - so a Tika server that
     * returns 404 after an upgrade, or a wrong baseUrl, would silently and permanently write off
     * every document it was given.
     * <p>
     * Which of the two exceptions it is decides whether the document pays for the failure: 503 is
     * about the server and must not be counted against the document, 422 is Tika refusing this
     * content and is.
     */
    @Test
    public void tikaErrorResponseMustNotBecomeMetasetContent() throws IOException {
        int[]      answeredStatus = {503};
        HttpServer brokenTika     = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        brokenTika.createContext("/tika", exchange -> {
            // read the upload, or the client sees a reset connection rather than our status:
            exchange.getRequestBody().readAllBytes();
            exchange.sendResponseHeaders(answeredStatus[0], -1);
            exchange.close();
        });
        brokenTika.start();
        try {
            CinnamonTikaConfig config = new CinnamonTikaConfig(
                    "http://127.0.0.1:" + brokenTika.getAddress().getPort(), true);
            TikaService tikaService = new TikaService(config, contentProviderService);
            try (FileInputStream bunnyStream = new FileInputStream(bun)) {
                TikaUnavailableException exception = assertThrows(TikaUnavailableException.class,
                        () -> tikaService.parseData(bunnyStream, bun.length(), imageFormat, osdId));
                assertTrue(exception.getMessage().contains("503"), exception.getMessage());
            }
            answeredStatus[0] = 422;
            try (FileInputStream bunnyStream = new FileInputStream(bun)) {
                TikaFailedException exception = assertThrows(TikaFailedException.class,
                        () -> tikaService.parseData(bunnyStream, bun.length(), imageFormat, osdId));
                assertTrue(exception.getMessage().contains("422"), exception.getMessage());
            }
        }
        finally {
            brokenTika.stop(0);
        }
    }

    /**
     * A Tika server that cannot be reached at all must not look like a document failure: TikaService
     * counts document failures against maxTikaAttempts and gives up at the limit, so counting an
     * outage would write off every pending document while Tika was down.
     */
    @Test
    public void unreachableTikaIsNotADocumentFailure() throws IOException {
        int closedPort;
        try (ServerSocket socket = new ServerSocket(0)) {
            closedPort = socket.getLocalPort();
        }
        CinnamonTikaConfig config = new CinnamonTikaConfig("http://127.0.0.1:" + closedPort, true);
        config.setRetryDelayInSeconds(1);
        config.setMaxRetryDelayInSeconds(1);
        TikaService tikaService = new TikaService(config, contentProviderService);
        try (FileInputStream bunnyStream = new FileInputStream(bun)) {
            FailsafeException exception = assertThrows(FailsafeException.class,
                    () -> tikaService.parseWithTika(bunnyStream, bun.length(), imageFormat, osdId));
            // this is the exception TikaService' run loop recognizes as "retry, do not count":
            assertInstanceOf(IOException.class, exception.getCause());
        }
    }

    @Test
    public void disabledTika() throws IOException {
        CinnamonTikaConfig cinnamonTikaConfig = new CinnamonTikaConfig(baseUrl, false);
        try (FileInputStream bunnyStream = new FileInputStream(bun)) {
            String bunData = new TikaService(cinnamonTikaConfig, contentProviderService)
                    .parseData(bunnyStream, bun.length(), imageFormat, osdId);
            assertEquals("<tikaIsDisabled/>", bunData);
        }
    }

}
