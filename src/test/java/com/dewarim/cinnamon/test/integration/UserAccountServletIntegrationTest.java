package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.CinnamonServer;
import com.dewarim.cinnamon.client.CinnamonClient;
import com.dewarim.cinnamon.client.CinnamonClientException;
import com.dewarim.cinnamon.model.LoginType;
import com.dewarim.cinnamon.model.UserAccount;
import com.dewarim.cinnamon.model.request.user.GetUserAccountRequest;
import com.dewarim.cinnamon.model.request.user.SetPasswordRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class UserAccountServletIntegrationTest extends CinnamonIntegrationTest {

    private static final Logger log = LogManager.getLogger(UserAccountServletIntegrationTest.class);


    @Test
    public void requestShouldHaveUserIdOrUsername() throws IOException {
        GetUserAccountRequest userInfoRequest  = new GetUserAccountRequest(null, null);
        HttpResponse          userInfoResponse = sendAdminRequest(UrlMapping.USER__GET, userInfoRequest);
        assertCinnamonError(userInfoResponse, ErrorCode.USER_INFO_REQUEST_WITHOUT_NAME_OR_ID);
    }

    @Test
    public void validRequestByUsername() throws IOException {
        UserAccount admin = client.getUser("admin");
        assertThat(admin.getName(), equalTo("admin"));
    }

    @Test
    public void requestByUserId() throws IOException {
        UserAccount userInfo = client.getUser(1L);
        assertThat(userInfo.getId(), equalTo(1L));
        assertThat(userInfo.getName(), equalTo("admin"));
    }

    @Test
    public void listUsers() throws IOException {
        List<UserAccount> users = client.listUsers();
        List<String>      names = Arrays.asList("admin", "doe", "deactivated user", "locked user");
        List<String> userNames = users.stream().map(UserAccount::getName).collect(Collectors.toList());
        assertTrue(userNames.containsAll(names));
        assertFalse(client.getUser("deactivated user").isActivated());
        assertTrue(client.getUser("locked user").isLocked());
    }

    @Test
    public void securityInfoIsFiltered() throws IOException {
        List<UserAccount> users = client.listUsers();
        assertTrue(users.stream().allMatch(user -> user.getPassword() == null
                && user.getToken() == null));
    }

    @Test
    public void requestForNonExistentUser() throws IOException {
        GetUserAccountRequest userInfoRequest  = new GetUserAccountRequest(123L, null);
        HttpResponse          userInfoResponse = sendAdminRequest(UrlMapping.USER__GET, userInfoRequest);
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
        SetPasswordRequest setPasswordRequest = new SetPasswordRequest(2L, "x");
        HttpResponse       response           = sendStandardRequest(UrlMapping.USER__SET_PASSWORD, setPasswordRequest);
        assertCinnamonError(response, ErrorCode.PASSWORD_TOO_SHORT);
    }

    @Test
    public void createUserHappyPath() throws IOException {
        String username = "new user";
        String password = "xxx12345";
        UserAccount user = new UserAccount(username, password, "A new user", "user@invalid.com",
                1L, LoginType.CINNAMON.name(), false, true);
        UserAccount userAccount = adminClient.createUser(user);
        assertEquals(username, userAccount.getName());
        assertEquals(user.getFullname(), userAccount.getFullname());
        assertEquals(user.getEmail(), userAccount.getEmail());
        assertEquals(user.getUiLanguageId(), userAccount.getUiLanguageId());
        assertEquals(user.getLoginType(), userAccount.getLoginType());
        assertEquals(user.isChangeTracking(), userAccount.isChangeTracking());
        assertEquals(user.isActivated(), userAccount.isActivated());

        var testClient = new CinnamonClient(adminClient.getPort(), adminClient.getHost(), adminClient.getProtocol(),
                username, password);
        testClient.connect();
        assertEquals(userAccount.getId(), testClient.getUser(userAccount.getId()).getId());
    }

    @Test
    public void createUserWithoutPassword() {
        String username = "new user without password";
        UserAccount user = new UserAccount(username, "", "A new user", "user@invalid.com",
                1L, LoginType.CINNAMON.name(), false, true);
        CinnamonClientException ex = assertThrows(CinnamonClientException.class, () -> adminClient.createUser(user));
        assertEquals(ErrorCode.INVALID_REQUEST, ex.getErrorCode());
    }

    @Test
    public void createUserWithExistingName(){
        UserAccount user = new UserAccount("admin", "a second admin!", "A new user", "user@invalid.com",
                1L, LoginType.CINNAMON.name(), false, true);
        CinnamonClientException ex = assertThrows(CinnamonClientException.class, () -> adminClient.createUser(user));
        assertEquals(ErrorCode.DB_INSERT_FAILED, ex.getErrorCode());
    }

    @Test
    public void createUserWithoutAdminAccess(){
        UserAccount user = new UserAccount("not-an-admin", "just-a-pass", "A new user", "user@invalid.com",
                1L, LoginType.CINNAMON.name(), false, true);
        CinnamonClientException ex = assertThrows(CinnamonClientException.class, () -> client.createUser(user));
        assertEquals(ErrorCode.REQUIRES_SUPERUSER_STATUS, ex.getErrorCode());
    }

    @Test
    public void createUserWithoutTooShortPassword() {
        String username = "new user with tiny password";
        UserAccount user = new UserAccount(username, "tiny", "A new user", "user@invalid.com",
                1L, LoginType.CINNAMON.name(), false, true);
        CinnamonClientException ex = assertThrows(CinnamonClientException.class, () -> adminClient.createUser(user));
        assertEquals(ErrorCode.PASSWORD_TOO_SHORT, ex.getErrorCode());
    }

    @Test
    public void updateUser() throws IOException {
        String username = "update user";
        String password = "12345678";
        UserAccount user = new UserAccount(username, password, "A new user", "user@invalid.com",
                1L, LoginType.CINNAMON.name(), false, true);
        UserAccount account = adminClient.createUser(user);
        account.setName("updated user");
        UserAccount userAccount = adminClient.updateUser(account);
        assertEquals(account.getName(), userAccount.getName());
    }

    @Test
    public void updateWithDuplicateName() throws IOException {
        UserAccount user = new UserAccount("duplicate-name-test", "xxx123456", "A new user", "user@invalid.com",
                1L, LoginType.CINNAMON.name(), false, true);
        UserAccount account = adminClient.createUser(user);
        account.setName("admin");

        CinnamonClientException ex = assertThrows(CinnamonClientException.class, () -> adminClient.updateUser(account));
        assertEquals(ErrorCode.DB_UPDATE_FAILED, ex.getErrorCode());
    }

    @Test
    public void updateWithoutChangingPassword() throws IOException {
        String password    = "xxx123456";
        String username    = "update-without-changing-password";
        String newUserName = "a new updated name";
        UserAccount user = new UserAccount(username, password, "A new user", "user@invalid.com",
                1L, LoginType.CINNAMON.name(), false, true);
        UserAccount account = adminClient.createUser(user);
        account.setName(newUserName);
        // passwords should always be filtered when returning a user:
        assertNull(account.getPassword());

        // submitting a null password should count as "do not update password"
        adminClient.updateUser(account);
        var testClient = new CinnamonClient(adminClient.getPort(), adminClient.getHost(), adminClient.getProtocol(), newUserName, password);
        testClient.connect();

        // submitting an empty password should also count as "do not update password"
        account.setPassword("");
        adminClient.updateUser(account);
        var testClient2 = new CinnamonClient(adminClient.getPort(), adminClient.getHost(), adminClient.getProtocol(), newUserName, password);
        testClient2.connect();
    }
}
