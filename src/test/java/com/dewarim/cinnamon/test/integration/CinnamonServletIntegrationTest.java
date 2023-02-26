package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.servlet.CinnamonServlet;
import com.dewarim.cinnamon.client.CinnamonClient;
import com.dewarim.cinnamon.model.response.CinnamonConnection;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Objects;

import static com.dewarim.cinnamon.ErrorCode.*;
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
        HttpResponse response = Request.Get("http://localhost:" + cinnamonTestPort + UrlMapping.CINNAMON__INFO.getPath()).execute().returnResponse();
        assertResponseOkay(response);
        CinnamonServlet.CinnamonVersion version         = mapper.readValue(response.getEntity().getContent(), CinnamonServlet.CinnamonVersion.class);
        CinnamonServlet                 servlet         = new CinnamonServlet();
        CinnamonServlet.CinnamonVersion cinnamonVersion = servlet.getCinnamonVersion();
        assertEquals(version.getBuild(), cinnamonVersion.getBuild());
        assertEquals(version.getVersion(), cinnamonVersion.getVersion());
    }
}
