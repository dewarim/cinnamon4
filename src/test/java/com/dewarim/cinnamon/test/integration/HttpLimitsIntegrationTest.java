package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.application.CinnamonServer;
import com.dewarim.cinnamon.configuration.HttpLimitsConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HTTP-layer hardening: request body size limit and no information disclosure on error pages.
 */
public class HttpLimitsIntegrationTest extends CinnamonIntegrationTest {

    private final HttpClient       httpClient = HttpClient.newHttpClient();
    private       HttpLimitsConfig originalConfig;

    @AfterEach
    public void restoreConfig() {
        if (originalConfig != null) {
            CinnamonServer.config.setHttpLimitsConfig(originalConfig);
            originalConfig = null;
        }
    }

    private void setLimits(HttpLimitsConfig config) {
        originalConfig = CinnamonServer.config.getHttpLimitsConfig();
        CinnamonServer.config.setHttpLimitsConfig(config);
    }

    @Test
    public void oversizedRequestBodyIsRejectedWith413() throws IOException, InterruptedException {
        HttpLimitsConfig limits = new HttpLimitsConfig();
        limits.setMaxRequestBodySize(1000);
        setLimits(limits);

        // unauthenticated endpoint: the limit must apply before any processing
        byte[]   body     = new byte[5000];
        HttpResponse<String> response = post(HOST + "/cinnamon/connect", body);

        assertEquals(413, response.statusCode());
        assertTrue(response.body().contains("REQUEST_PAYLOAD_TOO_LARGE"), () -> "body was: " + response.body());
    }

    @Test
    public void bodyWithinLimitIsAccepted() throws IOException, InterruptedException {
        HttpLimitsConfig limits = new HttpLimitsConfig();
        limits.setMaxRequestBodySize(1_000_000);
        setLimits(limits);

        // malformed but small body: reaches the servlet and fails there, i.e. it is NOT rejected as too large
        HttpResponse<String> response = post(HOST + "/cinnamon/connect",
                "<connectionRequest/>".getBytes(StandardCharsets.UTF_8));

        assertNotEquals(413, response.statusCode());
    }

    @Test
    public void errorPageDoesNotLeakServerVersionOrStackTrace() throws IOException, InterruptedException {
        HttpResponse<String> response = httpClient.send(
                HttpRequest.newBuilder(URI.create(HOST + "/does-not-exist")).GET().build(),
                HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
        assertTrue(response.headers().firstValue("Server").isEmpty(), "Server header must not be sent");
        String body = response.body();
        assertFalse(body.contains("at com.dewarim"), "error page must not contain a stack trace");
        assertFalse(body.contains("at org.eclipse"), "error page must not contain a stack trace");
        assertFalse(body.contains("Jetty("), "error page must not disclose the Jetty version");
        assertFalse(body.toLowerCase().contains("powered by jetty"), "error page must not disclose the server");
    }

    private HttpResponse<String> post(String url, byte[] body) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .header("Content-Type", "application/xml")
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
