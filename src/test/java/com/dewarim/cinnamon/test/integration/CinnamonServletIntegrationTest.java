package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.CinnamonServer;
import com.dewarim.cinnamon.application.servlet.CinnamonServlet;
import com.dewarim.cinnamon.client.CinnamonClient;
import com.dewarim.cinnamon.client.StandardResponse;
import com.dewarim.cinnamon.model.response.CinnamonConnection;
import com.dewarim.cinnamon.test.TestObjectHolder;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Objects;

import static com.dewarim.cinnamon.ErrorCode.*;
import static org.apache.hc.core5.http.ContentType.APPLICATION_XML;
import static org.apache.hc.core5.http.HttpHeaders.CONTENT_TYPE;
import static org.junit.jupiter.api.Assertions.*;

public class CinnamonServletIntegrationTest extends CinnamonIntegrationTest {

    private static final String UUID_PATTERN = "\\w+-\\w+-\\w+-\\w+-\\w+";

    @Test
    public void connectFailsWithoutValidUsername() {
        CinnamonClient aClient = new CinnamonClient(cinnamonTestPort, "localhost", "http", "invalid-user", "admin");
        assertClientError(aClient::connect, CONNECTION_FAIL_INVALID_USERNAME);
    }

    @Test
    public void connectFailsWithWrongPassword() {
        CinnamonClient aClient = new CinnamonClient(cinnamonTestPort, "localhost", "http", "admin", "invalid-password");
        assertClientError(aClient::connect, CONNECTION_FAIL_WRONG_PASSWORD);
    }

    @Test
    public void connectFailsWithLockedUser() {
        CinnamonClient aClient = new CinnamonClient(cinnamonTestPort, "localhost", "http", "locked user", "admin");
        assertClientError(aClient::connect, CONNECTION_FAIL_ACCOUNT_LOCKED);
    }

    @Test
    public void connectFailsWithInactiveUser() {
        CinnamonClient aClient = new CinnamonClient(cinnamonTestPort, "localhost", "http", "deactivated user", "admin");
        assertClientError(aClient::connect, CONNECTION_FAIL_ACCOUNT_INACTIVE);
    }

    @Test
    public void disconnectHappyPath() throws IOException {
        CinnamonClient myAdminClient = new CinnamonClient(cinnamonTestPort, "localhost", "http", "admin", "admin");
        assertTrue(myAdminClient.disconnect());
        assertClientError(myAdminClient::listObjectTypes, ErrorCode.AUTHENTICATION_FAIL_NO_SESSION_FOUND);
    }

    @Test
    public void disconnectNoTicket() {
        CinnamonClient myAdminClient = new CinnamonClient(cinnamonTestPort, "localhost", "http", "admin", "admin");
        myAdminClient.setTicket(null);
        myAdminClient.setGenerateTicketIfNull(false);
        assertClientError(myAdminClient::disconnect, ErrorCode.AUTHENTICATION_FAIL_NO_TICKET_GIVEN);
    }

    @Test
    public void disconnectInvalidTicket() {
        CinnamonClient myAdminClient = new CinnamonClient(cinnamonTestPort, "localhost", "http", "admin", "admin");
        myAdminClient.setTicket("totally invalid ticket");
        assertClientError(myAdminClient::disconnect, ErrorCode.SESSION_NOT_FOUND);
    }


    /**
     * When base class starts the test server, connect() is called automatically to
     * provide a ticket for all other API test classes.
     */
    @Test
    public void connectSucceedsWithValidUsernameAndPassword() {
        Objects.requireNonNull(ticket);
    }

    @Test
    public void connectWithFormatParameter() throws IOException {
        String plainTextSession = client.connect("admin", "admin", "text");
        assertNotNull(plainTextSession);
        assertTrue(plainTextSession.matches(UUID_PATTERN));
    }

    @Test
    public void connectWithoutFormatParameter() throws IOException {
        String rawResponse = client.connect("admin", "admin", null);
        assertNotNull(rawResponse);
        CinnamonConnection connection = mapper.readValue(rawResponse, CinnamonConnection.class);
        assertTrue(connection.getTicket().matches(UUID_PATTERN));
    }

    @Test
    public void infoPageReturnsBuildNumber() throws IOException {
        CinnamonServlet.CinnamonVersion version;
        try (StandardResponse response = httpClient.execute(
                ClassicRequestBuilder.get("http://localhost:" + cinnamonTestPort + UrlMapping.CINNAMON__INFO.getPath())
                        .setHeader(CONTENT_TYPE,APPLICATION_XML.toString()).build(),
                StandardResponse::new)) {
            assertResponseOkay(response);
            version = mapper.readValue(response.getEntity().getContent(), CinnamonServlet.CinnamonVersion.class);
        }
        CinnamonServlet                 servlet         = new CinnamonServlet();
        CinnamonServlet.CinnamonVersion cinnamonVersion = servlet.getCinnamonVersion();
        assertEquals(version.getBuild(), cinnamonVersion.getBuild());
        assertEquals(version.getVersion(), cinnamonVersion.getVersion());
    }

    @Test
    public void reloadLogging() throws IOException{
        var toh = new TestObjectHolder(client, userId);
        assertClientError(toh::reloadLogging, REQUIRES_SUPERUSER_STATUS);
        var adminToh = new TestObjectHolder(adminClient, adminId);
        assertClientError(adminToh::reloadLogging, NEED_EXTERNAL_LOGGING_CONFIG);
        CinnamonServer.config.getServerConfig().setLog4jConfigPath("log4j2-example.xml");
        adminToh.reloadLogging();
        CinnamonServer.config.getServerConfig().setLog4jConfigPath("src/test/resources/log4j2-test.xml");
        adminToh.reloadLogging();
    }
}
