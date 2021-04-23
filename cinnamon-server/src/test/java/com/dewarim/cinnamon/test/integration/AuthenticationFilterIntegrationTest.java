package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.CinnamonServer;
import com.dewarim.cinnamon.application.ErrorCode;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.dao.UserAccountDao;
import com.dewarim.cinnamon.model.UserAccount;
import com.dewarim.cinnamon.model.request.user.UserInfoRequest;
import com.dewarim.cinnamon.model.response.CinnamonError;
import com.dewarim.cinnamon.model.response.UserInfo;
import com.dewarim.cinnamon.model.response.UserWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.ibatis.session.SqlSession;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;


/**
 */
public class AuthenticationFilterIntegrationTest extends CinnamonIntegrationTest {


    @Test
    public void happyPathAuthentication() throws IOException {

        assertThat(ticket, is(notNullValue()));
        assertThat(ticket.matches("(?:[a-z0-9]+-){4}[a-z0-9]+"), is(true));

        // call an API method with ticket and valid request:
        UserInfoRequest userInfoRequest  = new UserInfoRequest(null, "admin");
        HttpResponse    userInfoResponse = sendAdminRequest(UrlMapping.USER__USER_INFO, userInfoRequest);
        assertThat(userInfoResponse.getStatusLine().getStatusCode(), equalTo(HttpServletResponse.SC_OK));
        UserInfo info = mapper.readValue(userInfoResponse.getEntity().getContent(), UserWrapper.class).getUsers().get(0);
        assertThat(info.getName(), equalTo("admin"));

    }

    @Test
    public void callingApiWithoutTicketIsForbidden() throws IOException {
        String userInfoRequest = mapper.writeValueAsString(new UserInfoRequest(null, "admin"));
        HttpResponse response = Request.Post("http://localhost:" + cinnamonTestPort + UrlMapping.USER__USER_INFO.getPath())
                .bodyString(userInfoRequest, ContentType.APPLICATION_XML)
                .execute().returnResponse();
        assertThat(response.getStatusLine().getStatusCode(), equalTo(HttpServletResponse.SC_FORBIDDEN));
    }

    @Test
    public void callApiWithExpiredSession() throws IOException, InterruptedException {
        String oldTicket             = ticket;
        long   sessionLengthInMillis = CinnamonServer.config.getSecurityConfig().getSessionLengthInMillis();
        CinnamonServer.config.getSecurityConfig().setSessionLengthInMillis(0);
        ticket = getAdminTicket();
        Thread.sleep(10);
        UserInfoRequest userInfoRequest = new UserInfoRequest(null, "admin");
        HttpResponse    response        = sendAdminRequest(UrlMapping.USER__USER_INFO, userInfoRequest);
        assertThat(response.getStatusLine().getStatusCode(), equalTo(HttpServletResponse.SC_FORBIDDEN));
        CinnamonError error = mapper.readValue(response.getEntity().getContent(), CinnamonError.class);
        assertThat(error.getCode(), equalTo(ErrorCode.AUTHENTICATION_FAIL_SESSION_EXPIRED.getCode()));

        // create new, not expired ticket for other tests
        ticket = oldTicket;
        CinnamonServer.config.getSecurityConfig().setSessionLengthInMillis(sessionLengthInMillis);
    }

    @Test
    public void callApiWithNonExistingSessionTicket() throws IOException {
        String userInfoRequest = mapper.writeValueAsString(new UserInfoRequest(null, "admin"));
        HttpResponse response = Request.Post("http://localhost:" + cinnamonTestPort + UrlMapping.USER__USER_INFO.getPath())
                .addHeader("ticket", " ")
                .bodyString(userInfoRequest, ContentType.APPLICATION_XML)
                .execute().returnResponse();
        assertThat(response.getStatusLine().getStatusCode(), equalTo(HttpServletResponse.SC_FORBIDDEN));
        CinnamonError error = mapper.readValue(response.getEntity().getContent(), CinnamonError.class);
        assertThat(error.getCode(), equalTo(ErrorCode.AUTHENTICATION_FAIL_NO_TICKET_GIVEN.getCode()));
    }

    @Test
    public void callApiWithTicketOfInvalidUser() throws IOException {
        // user has been deleted or is inactive
        UserAccountDao userDao = new UserAccountDao();
        UserAccount    admin   = userDao.getUserAccountByName("admin").orElseThrow(() -> new RuntimeException("admin not found"));
        admin.setActivated(false);
        userDao.changeUserActivationStatus(admin);
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        sqlSession.commit();

        UserInfoRequest userInfoRequest = new UserInfoRequest(null, "admin");
        HttpResponse    response        = sendAdminRequest(UrlMapping.USER__USER_INFO, userInfoRequest);
        assertThat(response.getStatusLine().getStatusCode(), equalTo(HttpServletResponse.SC_FORBIDDEN));
        CinnamonError error = mapper.readValue(response.getEntity().getContent(), CinnamonError.class);
        assertThat(error.getCode(), equalTo(ErrorCode.AUTHENTICATION_FAIL_USER_NOT_FOUND.getCode()));

        // restore active status for admin for further tests:
        admin.setActivated(true);
        userDao.changeUserActivationStatus(admin);
        sqlSession.commit();

    }
}
