package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.CinnamonServer;
import com.dewarim.cinnamon.model.request.user.ListUserInfoRequest;
import com.dewarim.cinnamon.model.request.user.SetPasswordRequest;
import com.dewarim.cinnamon.model.request.user.UserInfoRequest;
import com.dewarim.cinnamon.model.response.UserInfo;
import com.dewarim.cinnamon.model.response.UserWrapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

public class UserServletIntegrationTest extends CinnamonIntegrationTest {


    @Test
    public void requestShouldHaveUserIdOrUsername() throws IOException {
        UserInfoRequest userInfoRequest  = new UserInfoRequest(null, null);
        HttpResponse    userInfoResponse = sendAdminRequest(UrlMapping.USER__USER_INFO, userInfoRequest);
        assertCinnamonError(userInfoResponse, ErrorCode.USER_INFO_REQUEST_WITHOUT_NAME_OR_ID);
    }

    @Test
    public void validRequestByUsername() throws IOException {
        UserInfoRequest userInfoRequest  = new UserInfoRequest(null, "admin");
        HttpResponse    userInfoResponse = sendAdminRequest(UrlMapping.USER__USER_INFO, userInfoRequest);
        UserInfo        admin            = unwrapUsers(userInfoResponse, 1).get(0);
        assertThat(admin.getName(), equalTo("admin"));
    }

    @Test
    public void requestByUserId() throws IOException {
        UserInfo userInfo = client.getUser(1L);
        assertThat(userInfo.getId(), equalTo(1L));
        assertThat(userInfo.getName(), equalTo("admin"));

    }

    @Test
    public void listUsers() throws IOException {
        HttpResponse userInfoResponse = sendAdminRequest(UrlMapping.USER__LIST_USERS, new ListUserInfoRequest());

        List<String>   names = Arrays.asList("admin", "doe", "deactivated user", "locked user");
        List<UserInfo> users = unwrapUsers(userInfoResponse, 4);
        users.forEach(user -> assertTrue(names.contains(user.getName())));
        assertTrue(users.contains(new UserInfo(3L, "deactivated user", "CINNAMON", false, false, 1L, null, "inactive", true, false)));
        assertTrue(users.contains(new UserInfo(4L, "locked user", "CINNAMON", true, true, 1L, null, "locked", true, false)));
    }

    @Test
    public void requestForNonExistentUser() throws IOException {
        UserInfoRequest userInfoRequest  = new UserInfoRequest(123L, null);
        HttpResponse    userInfoResponse = sendAdminRequest(UrlMapping.USER__USER_INFO, userInfoRequest);
        assertCinnamonError(userInfoResponse, ErrorCode.USER_ACCOUNT_NOT_FOUND);
    }

    @Test
    public void setUsersOwnPassword() throws IOException {
        SetPasswordRequest setPasswordRequest = new SetPasswordRequest(2L, "testTest");
        HttpResponse       response           = sendStandardRequest(UrlMapping.USER__SET_PASSWORD, setPasswordRequest);
        assertResponseOkay(response);
        String url = "http://localhost:" + cinnamonTestPort + UrlMapping.CINNAMON__CONNECT.getPath();
        HttpResponse ticketResponse = Request.Post(url)
                .bodyForm(Form.form().add("user", "doe").add(PASSWORD_PARAMETER_NAME, "testTest").build())
                .execute().returnResponse();
        assertResponseOkay(ticketResponse);

        // cleanup:
        CinnamonServer.config.getSecurityConfig().setMinimumPasswordLength(4);
        SetPasswordRequest setPasswordRequest2 = new SetPasswordRequest(2L, "admin");
        HttpResponse       response2           = sendStandardRequest(UrlMapping.USER__SET_PASSWORD, setPasswordRequest);
        CinnamonServer.config.getSecurityConfig().setMinimumPasswordLength(8);
    }

    @Test
    public void setOtherUsersPassword() throws IOException {
        SetPasswordRequest setPasswordRequest = new SetPasswordRequest(1L, "testTest");
        HttpResponse       response           = sendStandardRequest(UrlMapping.USER__SET_PASSWORD, setPasswordRequest);
        assertCinnamonError(response, ErrorCode.FORBIDDEN);
    }

    @Test
    public void setOtherUsersPasswordAsAdmin() throws IOException {
        SetPasswordRequest setPasswordRequest = new SetPasswordRequest(3L, "testTest");
        HttpResponse       response           = sendAdminRequest(UrlMapping.USER__SET_PASSWORD, setPasswordRequest);
        assertResponseOkay(response);
    }

    @Test
    public void setTooShortPassword() throws IOException {
        SetPasswordRequest setPasswordRequest = new SetPasswordRequest(2L, "test");
        HttpResponse       response           = sendStandardRequest(UrlMapping.USER__SET_PASSWORD, setPasswordRequest);
        assertCinnamonError(response, ErrorCode.PASSWORD_TOO_SHORT);
    }

    private List<UserInfo> unwrapUsers(HttpResponse response, Integer expectedSize) throws IOException {
        assertResponseOkay(response);
        List<UserInfo> users = mapper.readValue(response.getEntity().getContent(), UserWrapper.class).getUsers();
        if (expectedSize != null) {
            assertNotNull(users);
            assertFalse(users.isEmpty());
            assertThat(users.size(), equalTo(expectedSize));
        }
        return users;
    }
}
