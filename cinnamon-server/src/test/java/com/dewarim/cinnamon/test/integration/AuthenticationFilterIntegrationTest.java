package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.application.UrlMapping;
import com.dewarim.cinnamon.model.request.UserInfoRequest;
import com.dewarim.cinnamon.model.response.CinnamonError;
import com.dewarim.cinnamon.model.response.Connection;
import com.dewarim.cinnamon.model.response.UserInfo;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.junit.Test;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;


/**
 */
public class AuthenticationFilterIntegrationTest extends CinnamonIntegrationTest {

    public static String getAdminTicket() throws IOException{
        String tokenRequestResult = Request.Post("http://localhost:" + cinnamonTestPort + "/cinnamon/connect")
                .bodyForm(Form.form().add("user", "admin").add("pwd", "admin").build())
                .execute().returnContent().asString();
        XmlMapper mapper = new XmlMapper();
        Connection connection = mapper.readValue(tokenRequestResult, Connection.class);
        return connection.getTicket();
    }
    
    @Test
    public void testAuthentication() throws IOException {

        // connect to get ticket
        String tokenRequestResult = Request.Post("http://localhost:" + cinnamonTestPort + "/cinnamon/connect")
                .bodyForm(Form.form().add("user", "admin").add("pwd", "admin").build())
                .execute().returnContent().asString();

        XmlMapper mapper = new XmlMapper();

        Connection connection = mapper.readValue(tokenRequestResult, Connection.class);
        assertThat(connection.getTicket(), is(notNullValue()));
        assertThat(connection.getTicket().matches("(?:[a-z0-9]+-){4}[a-z0-9]+"), is(true));

        System.out.println(tokenRequestResult);
        String userPath = UrlMapping.USER__USER_INFO.getPath();
        
        // call an API method without ticket:
        String userInfoRequest = mapper.writeValueAsString(new UserInfoRequest(null, null));
        HttpResponse response = Request.Post("http://localhost:" + cinnamonTestPort + userPath)
                .bodyString(userInfoRequest, ContentType.APPLICATION_XML)
                .execute().returnResponse();
        assertThat(response.getStatusLine().getStatusCode(), equalTo(HttpServletResponse.SC_FORBIDDEN));

        // call with broken request (TODO: move to UserServletIntegrationTest etc)
        HttpResponse errorResponse = Request.Post("http://localhost:" + cinnamonTestPort + userPath)
                .addHeader("ticket", connection.getTicket())
                .bodyString(userInfoRequest, ContentType.APPLICATION_XML)
                .execute().returnResponse();
        assertThat(errorResponse.getStatusLine().getStatusCode(), equalTo(HttpServletResponse.SC_BAD_REQUEST));
        CinnamonError error = mapper.readValue(errorResponse.getEntity().getContent(), CinnamonError.class);
        assertThat(error.getCode(), equalTo("error.userInfoRequest.missing.id.or.name"));

        // call an API method with ticket and valid request:
        String validUserInfoRequest = mapper.writeValueAsString(new UserInfoRequest(null,"admin"));
        HttpResponse userInfoResponse = Request.Post("http://localhost:" + cinnamonTestPort + userPath)
                .addHeader("ticket", connection.getTicket())
                .bodyString(validUserInfoRequest, ContentType.APPLICATION_XML)
                .execute().returnResponse();
        assertThat(userInfoResponse.getStatusLine().getStatusCode(), equalTo(HttpServletResponse.SC_OK));
        // TODO: always return objects wrapped in the list parent (for example: <users><user/><user/></users>)
        UserInfo info = mapper.readValue(userInfoResponse.getEntity().getContent(), UserInfo.class);
        assertThat(info.getName(), equalTo("admin"));

    }

}
