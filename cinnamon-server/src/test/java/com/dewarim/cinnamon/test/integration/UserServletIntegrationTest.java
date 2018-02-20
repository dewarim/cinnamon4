package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.application.ErrorCode;
import com.dewarim.cinnamon.application.UrlMapping;
import com.dewarim.cinnamon.model.request.UserInfoRequest;
import com.dewarim.cinnamon.model.response.CinnamonError;
import com.dewarim.cinnamon.model.response.UserInfo;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class UserServletIntegrationTest extends CinnamonIntegrationTest {


    @Test
    public void requestShouldHaveUserIdOrUsername() throws IOException {

        String userInfoRequest = mapper.writeValueAsString(new UserInfoRequest(null, null));
        HttpResponse userInfoResponse = Request.Post("http://localhost:" + cinnamonTestPort + UrlMapping.USER__USER_INFO.getPath())
                .addHeader("ticket", ticket)
                .bodyString(userInfoRequest, ContentType.APPLICATION_XML)
                .execute().returnResponse();
        assertThat(userInfoResponse.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_BAD_REQUEST));
        CinnamonError cinnamonError = mapper.readValue(userInfoResponse.getEntity().getContent(), CinnamonError.class);

        assertThat(cinnamonError.getCode(), equalTo(ErrorCode.USER_INFO_REQUEST_WITHOUT_NAME_OR_ID.getCode()));
    }

    @Test
    public void validRequestByUsername() throws IOException {
        String userInfoRequest = mapper.writeValueAsString(new UserInfoRequest(null, "admin"));
        HttpResponse userInfoResponse = Request.Post("http://localhost:" + cinnamonTestPort + UrlMapping.USER__USER_INFO.getPath())
                .addHeader("ticket", ticket)
                .bodyString(userInfoRequest, ContentType.APPLICATION_XML)
                .execute().returnResponse();
        assertThat(userInfoResponse.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_OK));
        UserInfo info = mapper.readValue(userInfoResponse.getEntity().getContent(), UserInfo.class);
        assertThat(info.getName(), equalTo("admin"));
    }

    @Test
    public void requestByUserId() throws IOException {
        String userInfoRequest = mapper.writeValueAsString(new UserInfoRequest(1L, null));
        HttpResponse userInfoResponse = Request.Post("http://localhost:" + cinnamonTestPort + UrlMapping.USER__USER_INFO.getPath())
                .addHeader("ticket", ticket)
                .bodyString(userInfoRequest, ContentType.APPLICATION_XML)
                .execute().returnResponse();
        assertThat(userInfoResponse.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_OK));
        UserInfo info = mapper.readValue(userInfoResponse.getEntity().getContent(), UserInfo.class);
        assertThat(info.getId(), equalTo(1L));

    }

    @Test
    public void requestForNonExistentUser() throws IOException {
        String userInfoRequest = mapper.writeValueAsString(new UserInfoRequest(123L, null));
        HttpResponse userInfoResponse = Request.Post("http://localhost:" + cinnamonTestPort + UrlMapping.USER__USER_INFO.getPath())
                .addHeader("ticket", ticket)
                .bodyString(userInfoRequest, ContentType.APPLICATION_XML)
                .execute().returnResponse();
        assertThat(userInfoResponse.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_BAD_REQUEST));
        CinnamonError cinnamonError = mapper.readValue(userInfoResponse.getEntity().getContent(), CinnamonError.class);
        
        assertThat(cinnamonError.getCode(), equalTo(ErrorCode.USER_ACCOUNT_NOT_FOUND.getCode()));
    }

}
