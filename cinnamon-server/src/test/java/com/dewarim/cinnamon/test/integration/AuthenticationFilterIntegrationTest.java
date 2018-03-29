package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.application.ErrorCode;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.application.UrlMapping;
import com.dewarim.cinnamon.dao.SessionDao;
import com.dewarim.cinnamon.dao.UserAccountDao;
import com.dewarim.cinnamon.model.Session;
import com.dewarim.cinnamon.model.UserAccount;
import com.dewarim.cinnamon.model.request.UserInfoRequest;
import com.dewarim.cinnamon.model.response.CinnamonError;
import com.dewarim.cinnamon.model.response.UserInfo;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.ibatis.session.SqlSession;
import org.junit.Test;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;


/**
 */
public class AuthenticationFilterIntegrationTest extends CinnamonIntegrationTest {


    @Test
    public void happyPathAuthentication() throws IOException {

        assertThat(ticket, is(notNullValue()));
        assertThat(ticket.matches("(?:[a-z0-9]+-){4}[a-z0-9]+"), is(true));
        
        String userPath = UrlMapping.USER__USER_INFO.getPath();

        // call an API method with ticket and valid request:
        String validUserInfoRequest = mapper.writeValueAsString(new UserInfoRequest(null, "admin"));
        HttpResponse userInfoResponse = Request.Post("http://localhost:" + cinnamonTestPort + userPath)
                .addHeader("ticket", ticket)
                .bodyString(validUserInfoRequest, ContentType.APPLICATION_XML)
                .execute().returnResponse();
        assertThat(userInfoResponse.getStatusLine().getStatusCode(), equalTo(HttpServletResponse.SC_OK));
        // TODO: always return objects wrapped in the list parent (for example: <users><user/><user/></users>)
        UserInfo info = mapper.readValue(userInfoResponse.getEntity().getContent(), UserInfo.class);
        assertThat(info.getName(), equalTo("admin"));

    }
    
    @Test
    public void callingApiWithoutTicketIsForbidden() throws IOException{
        String userInfoRequest = mapper.writeValueAsString(new UserInfoRequest(null, "admin"));
        HttpResponse response = Request.Post("http://localhost:" + cinnamonTestPort + UrlMapping.USER__USER_INFO.getPath())
                .bodyString(userInfoRequest, ContentType.APPLICATION_XML)
                .execute().returnResponse();
        assertThat(response.getStatusLine().getStatusCode(), equalTo(HttpServletResponse.SC_FORBIDDEN));
    }
    
    @Test
    public void callApiWithExpiredSession() throws IOException{
        SessionDao dao = new SessionDao();
        Session cinnamonSession = dao.getSessionByTicket(ticket);
        Date currentExpiration = cinnamonSession.getExpires();
        cinnamonSession.setExpires(new Date(1000000));
        dao.update(cinnamonSession);
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        sqlSession.commit();

        String userInfoRequest = mapper.writeValueAsString(new UserInfoRequest(null, "admin"));
        HttpResponse response = Request.Post("http://localhost:" + cinnamonTestPort + UrlMapping.USER__USER_INFO.getPath())
                .addHeader("ticket", ticket)
                .bodyString(userInfoRequest, ContentType.APPLICATION_XML)
                .execute().returnResponse();
        assertThat(response.getStatusLine().getStatusCode(), equalTo(HttpServletResponse.SC_FORBIDDEN));
        CinnamonError error = mapper.readValue(response.getEntity().getContent(), CinnamonError.class);
        assertThat(error.getCode(), equalTo(ErrorCode.AUTHENTICATION_FAIL_SESSION_EXPIRED.getCode()));
        
        // restore old expiration date for other tests
        cinnamonSession.setExpires(currentExpiration);
        dao.update(cinnamonSession);
        sqlSession.commit();
    }
    
    @Test
    public void callApiWithNonExistingSessionTicket() throws IOException{
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
    public void callApiWithTicketOfInvalidUser() throws IOException{
        // user has been deleted or is inactive
        UserAccountDao userDao = new UserAccountDao();
        UserAccount admin = userDao.getUserAccountByName("admin");
        admin.setActivated(false);
        userDao.changeUserActivationStatus(admin);
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        sqlSession.commit();

        String userInfoRequest = mapper.writeValueAsString(new UserInfoRequest(null, "admin"));
        HttpResponse response = Request.Post("http://localhost:" + cinnamonTestPort + UrlMapping.USER__USER_INFO.getPath())
                .addHeader("ticket", ticket)
                .bodyString(userInfoRequest, ContentType.APPLICATION_XML)
                .execute().returnResponse();
        assertThat(response.getStatusLine().getStatusCode(), equalTo(HttpServletResponse.SC_FORBIDDEN));
        CinnamonError error = mapper.readValue(response.getEntity().getContent(), CinnamonError.class);
        assertThat(error.getCode(), equalTo(ErrorCode.AUTHENTICATION_FAIL_USER_NOT_FOUND.getCode()));
        
        // restore active status for admin for further tests:
        admin.setActivated(true);
        userDao.changeUserActivationStatus(admin);
        sqlSession.commit();
        
    }
}
