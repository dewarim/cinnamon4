package com.dewarim.cinnamon.test;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A stand-in for the Apache Tika server: answers PUT /tika with whatever the test queued up, so a
 * test can play out a sequence like "rejected, rejected, then accepted" and watch what TikaService
 * does in between.
 * <p>
 * This is com.sun.net.httpserver, the exported API of the jdk.httpserver module rather than an
 * internal class, so it costs no dependency. The project dropped its one mock-server dependency on
 * purpose (see MicroserviceChangeTriggerIntegrationTest); a stub this small does not need one back.
 * It binds port 0, so parallel or repeated runs cannot collide.
 */
public class FakeTikaServer implements AutoCloseable {

    private final HttpServer      server;
    private final Queue<Response> responses        = new ConcurrentLinkedQueue<>();
    private final List<String>    receivedContentTypes = new CopyOnWriteArrayList<>();

    private record Response(int status, String body) {
    }

    public FakeTikaServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/tika", exchange -> {
            receivedContentTypes.add(String.valueOf(exchange.getRequestHeaders().getFirst("Content-Type")));
            // The upload has to be read before answering: a response sent while the request body is
            // still in flight reaches the client as a reset connection instead of a status code.
            exchange.getRequestBody().readAllBytes();
            Response response = responses.poll();
            if (response == null) {
                // a test that queued fewer responses than the requests it triggers should say so
                // rather than quietly receive something plausible:
                send(exchange, 500, "<noResponseQueued/>");
                return;
            }
            send(exchange, response.status(), response.body());
        });
        server.start();
    }

    private void send(com.sun.net.httpserver.HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body == null ? new byte[0] : body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "text/xml; charset=UTF-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    /**
     * Answer the next request with an HTTP error, the way a Tika server does for a document it
     * cannot handle, or after an upgrade that changed its endpoints.
     */
    public FakeTikaServer enqueueError(int status) {
        responses.add(new Response(status, "<error/>"));
        return this;
    }

    /**
     * Answer the next request with metadata.
     */
    public FakeTikaServer enqueueMetadata(String xml) {
        responses.add(new Response(200, xml));
        return this;
    }

    public String getBaseUrl() {
        return "http://127.0.0.1:" + server.getAddress().getPort();
    }

    public int getRequestCount() {
        return receivedContentTypes.size();
    }

    public List<String> getReceivedContentTypes() {
        return List.copyOf(receivedContentTypes);
    }

    @Override
    public void close() {
        server.stop(0);
    }
}
