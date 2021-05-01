package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.servlet.CinnamonServlet;
import com.dewarim.cinnamon.model.response.CinnamonError;
import com.dewarim.cinnamon.model.response.CinnamonErrorWrapper;
import com.dewarim.cinnamon.model.response.DisconnectResponse;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CinnamonServletIntegrationTest extends CinnamonIntegrationTest {

    @Test
    public void connectFailsWithoutValidUsername() throws IOException {
        String url = "http://localhost:" + cinnamonTestPort + UrlMapping.CINNAMON__CONNECT.getPath();
        HttpResponse response = Request.Post(url)
                .bodyForm(Form.form().add("user", "invalid-user").add(PASSWORD_PARAMETER_NAME, "admin").build())
                .execute().returnResponse();
        assertThat(response.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_UNAUTHORIZED));

        CinnamonError error = mapper.readValue(response.getEntity().getContent(), CinnamonErrorWrapper.class).getErrors().get(0);
        assertThat(error.getCode(), equalTo(ErrorCode.CONNECTION_FAIL_INVALID_USERNAME.getCode()));
    }

    @Test
    public void connectFailsWithWrongPassword() throws IOException {
        String url = "http://localhost:" + cinnamonTestPort + UrlMapping.CINNAMON__CONNECT.getPath();
        HttpResponse response = Request.Post(url)
                .bodyForm(Form.form().add("user", "admin").add(PASSWORD_PARAMETER_NAME, "invalid").build())
                .execute().returnResponse();
        assertThat(response.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_UNAUTHORIZED));

        CinnamonError error = mapper.readValue(response.getEntity().getContent(), CinnamonErrorWrapper.class).getErrors().get(0);
        assertThat(error.getCode(), equalTo(ErrorCode.CONNECTION_FAIL_WRONG_PASSWORD.getCode()));
    }

    @Test
    public void connectFailsWithLockedUser() throws IOException {
        String url = "http://localhost:" + cinnamonTestPort + UrlMapping.CINNAMON__CONNECT.getPath();
        HttpResponse response = Request.Post(url)
                .bodyForm(Form.form().add("user", "locked user").add(PASSWORD_PARAMETER_NAME, "admin").build())
                .execute().returnResponse();
        assertThat(response.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_UNAUTHORIZED));

        CinnamonError error = mapper.readValue(response.getEntity().getContent(), CinnamonErrorWrapper.class).getErrors().get(0);
        assertThat(error.getCode(), equalTo(ErrorCode.CONNECTION_FAIL_ACCOUNT_LOCKED.getCode()));
    }

    @Test
    public void connectFailsWithInactiveUser() throws IOException {
        String url = "http://localhost:" + cinnamonTestPort + UrlMapping.CINNAMON__CONNECT.getPath();
        HttpResponse response = Request.Post(url)
                .bodyForm(Form.form().add("user", "deactivated user").add(PASSWORD_PARAMETER_NAME, "admin").build())
                .execute().returnResponse();
        assertThat(response.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_UNAUTHORIZED));

        CinnamonError error = mapper.readValue(response.getEntity().getContent(), CinnamonErrorWrapper.class).getErrors().get(0);
        assertThat(error.getCode(), equalTo(ErrorCode.CONNECTION_FAIL_ACCOUNT_INACTIVE.getCode()));
    }

    @Test
    public void disconnectHappyPath() throws IOException {
        HttpResponse response = sendAdminRequest(UrlMapping.CINNAMON__DISCONNECT);
        assertResponseOkay(response);
        DisconnectResponse disconnectResponse = mapper.readValue(response.getEntity().getContent(), DisconnectResponse.class);
        assertTrue(disconnectResponse.isDisconnectSuccessful());

        HttpResponse verifyDisconnect = sendAdminRequest(UrlMapping.ACL__LIST);
        assertCinnamonError(verifyDisconnect, ErrorCode.AUTHENTICATION_FAIL_NO_SESSION_FOUND);

        ticket = getAdminTicket();
    }

    @Test
    public void disconnectNoTicket() throws IOException {
        String adminTicket = ticket;
        ticket = null;
        String       url      = "http://localhost:" + cinnamonTestPort + UrlMapping.CINNAMON__DISCONNECT.getPath();
        HttpResponse response = Request.Post(url).execute().returnResponse();
        assertCinnamonError(response, ErrorCode.SESSION_NOT_FOUND);
        ticket = adminTicket;
    }

    @Test
    public void disconnectInvalidTicket() throws IOException {
        String adminTicket = ticket;
        ticket = "totally invalid ticket";
        String       url      = "http://localhost:" + cinnamonTestPort + UrlMapping.CINNAMON__DISCONNECT.getPath();
        HttpResponse response = Request.Post(url).execute().returnResponse();
        assertCinnamonError(response, ErrorCode.SESSION_NOT_FOUND);
        ticket = adminTicket;
    }


    /**
     * When base class starts the test server, connect() is called automatically to
     * provide a ticket for all other API test classes.
     */
    @Test
    public void connectSucceedsWithValidUsernameAndPassword() {
        assertThat(ticket, notNullValue());
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
