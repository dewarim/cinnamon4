package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.application.ErrorCode;
import com.dewarim.cinnamon.application.UrlMapping;
import com.dewarim.cinnamon.model.request.UserInfoRequest;
import com.dewarim.cinnamon.model.response.UserInfo;
import com.dewarim.cinnamon.model.response.UserWrapper;
import org.apache.http.HttpResponse;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class UserServletIntegrationTest extends CinnamonIntegrationTest {


    @Test
    public void requestShouldHaveUserIdOrUsername() throws IOException {
        UserInfoRequest userInfoRequest = new UserInfoRequest(null, null);
        HttpResponse userInfoResponse = sendAdminRequest(UrlMapping.USER__USER_INFO,userInfoRequest);
        assertCinnamonError(userInfoResponse,ErrorCode.USER_INFO_REQUEST_WITHOUT_NAME_OR_ID);
    }

    @Test
    public void validRequestByUsername() throws IOException {
        UserInfoRequest userInfoRequest = new UserInfoRequest(null, "admin");
        HttpResponse userInfoResponse = sendAdminRequest(UrlMapping.USER__USER_INFO,userInfoRequest);
        UserInfo admin = unwrapUsers(userInfoResponse,1).get(0);
        assertThat(admin.getName(), equalTo("admin"));
    }

    @Test
    public void requestByUserId() throws IOException {
        UserInfoRequest userInfoRequest = new UserInfoRequest(1L, null);
        HttpResponse userInfoResponse = sendAdminRequest(UrlMapping.USER__USER_INFO,userInfoRequest);
        UserInfo admin = unwrapUsers(userInfoResponse,1).get(0);
        assertThat(admin.getId(), equalTo(1L));

    }

    @Test
    public void requestForNonExistentUser() throws IOException {
        UserInfoRequest userInfoRequest = new UserInfoRequest(123L, null);
        HttpResponse userInfoResponse = sendAdminRequest(UrlMapping.USER__USER_INFO,userInfoRequest);
        assertCinnamonError(userInfoResponse,ErrorCode.USER_ACCOUNT_NOT_FOUND);
    }
    
    private List<UserInfo> unwrapUsers(HttpResponse response, Integer expectedSize) throws IOException {
        assertResponseOkay(response);
        List<UserInfo> users = mapper.readValue(response.getEntity().getContent(),UserWrapper.class).getUsers();
        if (expectedSize != null) {
            assertNotNull(users);
            assertFalse(users.isEmpty());
            MatcherAssert.assertThat(users.size(), equalTo(expectedSize));
        }
        return users;
    }
}
