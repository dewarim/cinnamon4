package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.CinnamonServer;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.client.StandardResponse;
import com.dewarim.cinnamon.dao.UserAccountDao;
import com.dewarim.cinnamon.model.UserAccount;
import com.dewarim.cinnamon.model.request.user.GetUserAccountRequest;
import com.dewarim.cinnamon.model.response.CinnamonError;
import com.dewarim.cinnamon.model.response.CinnamonErrorWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.apache.hc.core5.http.ContentType.APPLICATION_XML;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;


/**
 *
 */
public class AuthenticationFilterIntegrationTest extends CinnamonIntegrationTest {


    @Test
    public void happyPathAuthentication() throws IOException {

        assertThat(ticket, is(notNullValue()));
        assertThat(ticket.matches("(?:[a-z0-9]+-){4}[a-z0-9]+"), is(true));

        // call an API method with ticket and valid request:
        var user = client.getUser("admin");
        assertThat(user.getName(), equalTo("admin"));

    }

    @Test
    public void callingApiWithoutTicketIsForbidden() throws IOException {
        String userInfoRequest = mapper.writeValueAsString(new GetUserAccountRequest(null, "admin"));
        String url             = "http://localhost:" + cinnamonTestPort + UrlMapping.USER__GET.getPath();
        try (StandardResponse response = httpClient.execute(ClassicRequestBuilder.post(url)
                .setHeader("Content-type", APPLICATION_XML.toString())
                .setEntity(userInfoRequest)
                .build(), StandardResponse::new )) {
            assertThat(response.getCode(), equalTo(HttpServletResponse.SC_FORBIDDEN));
        }
    }

    @Test
    public void callApiWithExpiredSession() throws IOException, InterruptedException, ParseException {
        String oldTicket             = ticket;
        long   sessionLengthInMillis = CinnamonServer.config.getSecurityConfig().getSessionLengthInMillis();
        CinnamonServer.config.getSecurityConfig().setSessionLengthInMillis(0);
        ticket = getAdminTicket();
        Thread.sleep(10);
        GetUserAccountRequest userInfoRequest = new GetUserAccountRequest(null, "admin");
        ClassicHttpResponse   response        = sendAdminRequest(UrlMapping.USER__GET, userInfoRequest);
        assertThat(response.getCode(), equalTo(HttpServletResponse.SC_FORBIDDEN));
        CinnamonError error = mapper.readValue(response.getEntity().getContent(), CinnamonErrorWrapper.class).getErrors().getFirst();
        assertThat(error.getCode(), equalTo(ErrorCode.AUTHENTICATION_FAIL_SESSION_EXPIRED.getCode()));

        // create new, not expired ticket for other tests
        ticket = oldTicket;
        CinnamonServer.config.getSecurityConfig().setSessionLengthInMillis(sessionLengthInMillis);
    }

    @Test
    public void callApiWithNonExistingSessionTicket() throws IOException {
        String userInfoRequest = mapper.writeValueAsString(new GetUserAccountRequest(null, "admin"));
        String url             = "http://localhost:" + cinnamonTestPort + UrlMapping.USER__GET.getPath();
        try (StandardResponse response = httpClient.execute(ClassicRequestBuilder.post(url)
                .addHeader("ticket", " ")
                .setEntity(userInfoRequest, APPLICATION_XML)
                .build(), StandardResponse::new)) {
            assertThat(response.getCode(), equalTo(HttpServletResponse.SC_FORBIDDEN));
            CinnamonError error = mapper.readValue(response.getEntity().getContent(), CinnamonErrorWrapper.class).getErrors().getFirst();
            assertThat(error.getCode(), equalTo(ErrorCode.AUTHENTICATION_FAIL_NO_TICKET_GIVEN.getCode()));
        }
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

        GetUserAccountRequest userInfoRequest = new GetUserAccountRequest(null, "admin");
        CinnamonError         error;
        try (ClassicHttpResponse response = sendAdminRequest(UrlMapping.USER__GET, userInfoRequest)) {
            assertThat(response.getCode(), equalTo(HttpServletResponse.SC_FORBIDDEN));
            error = mapper.readValue(response.getEntity().getContent(), CinnamonErrorWrapper.class).getErrors().getFirst();
        }
        assertThat(error.getCode(), equalTo(ErrorCode.AUTHENTICATION_FAIL_USER_NOT_FOUND.getCode()));

        // restore active status for admin for further tests:
        admin.setActivated(true);
        userDao.changeUserActivationStatus(admin);
        sqlSession.commit();

    }
}
