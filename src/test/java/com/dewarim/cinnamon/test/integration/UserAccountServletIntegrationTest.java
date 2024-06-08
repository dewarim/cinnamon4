package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.DefaultPermission;
import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.CinnamonServer;
import com.dewarim.cinnamon.client.CinnamonClient;
import com.dewarim.cinnamon.client.StandardResponse;
import com.dewarim.cinnamon.model.Folder;
import com.dewarim.cinnamon.model.LoginType;
import com.dewarim.cinnamon.model.ObjectSystemData;
import com.dewarim.cinnamon.model.UserAccount;
import com.dewarim.cinnamon.model.request.user.GetUserAccountRequest;
import com.dewarim.cinnamon.model.request.user.SetPasswordRequest;
import com.dewarim.cinnamon.test.TestObjectHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.dewarim.cinnamon.DefaultPermission.BROWSE;
import static com.dewarim.cinnamon.ErrorCode.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class UserAccountServletIntegrationTest extends CinnamonIntegrationTest {

    private static final Logger log = LogManager.getLogger(UserAccountServletIntegrationTest.class);


    @Test
    public void requestShouldHaveUserIdOrUsername() throws IOException {
        GetUserAccountRequest userInfoRequest = new GetUserAccountRequest(null, null);
        StandardResponse userInfoResponse = sendAdminRequest(UrlMapping.USER__GET, userInfoRequest);
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
        List<String> names = Arrays.asList("admin", "doe", "deactivated user", "locked user");
        List<String> userNames = users.stream().map(UserAccount::getName).toList();
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
        GetUserAccountRequest userInfoRequest = new GetUserAccountRequest(123L, null);
        StandardResponse userInfoResponse = sendAdminRequest(UrlMapping.USER__GET, userInfoRequest);
        assertCinnamonError(userInfoResponse, ErrorCode.USER_ACCOUNT_NOT_FOUND);
    }

    @Test
    public void setUsersOwnPassword() throws IOException {
        String newPassword = "testTest";
        SetPasswordRequest setPasswordRequest = new SetPasswordRequest(2L, newPassword );
        StandardResponse response = sendStandardRequest(UrlMapping.USER__SET_PASSWORD, setPasswordRequest);
        assertResponseOkay(response);
        String rawResponse = client.connect("doe", newPassword, "xml");
        assertNotNull(rawResponse);

        // cleanup:
        CinnamonServer.config.getSecurityConfig().setMinimumPasswordLength(4);
        SetPasswordRequest setPasswordRequest2 = new SetPasswordRequest(2L, "admin");
        try (StandardResponse r = sendStandardRequest(UrlMapping.USER__SET_PASSWORD, setPasswordRequest2)) {
            CinnamonServer.config.getSecurityConfig().setMinimumPasswordLength(8);
        }
    }

    @Test
    public void loginWithExpiredPassword() throws IOException {
        TestObjectHolder toh = new TestObjectHolder(adminClient, userId)
                .createUser();
        UserAccount user = toh.user;
        new CinnamonClient(client, user.getName(), toh.newUserPassword).connect();
        user.setPasswordExpired(true);
        adminClient.updateUser(user);
        assertClientError(() -> new CinnamonClient(client, user.getName(), toh.newUserPassword).connect(),
                ErrorCode.PASSWORD_IS_EXPIRED);
    }

    @Test
    public void setOtherUsersPassword() throws IOException {
        SetPasswordRequest setPasswordRequest = new SetPasswordRequest(1L, "testTest");
        StandardResponse response = sendStandardRequest(UrlMapping.USER__SET_PASSWORD, setPasswordRequest);
        assertCinnamonError(response, ErrorCode.FORBIDDEN);
    }

    @Test
    public void setOtherUsersPasswordAsAdmin() throws IOException {
        SetPasswordRequest setPasswordRequest = new SetPasswordRequest(3L, "testTest");
        StandardResponse response = sendAdminRequest(UrlMapping.USER__SET_PASSWORD, setPasswordRequest);
        assertResponseOkay(response);
    }

    @Test
    public void setTooShortPassword() throws IOException {
        SetPasswordRequest setPasswordRequest = new SetPasswordRequest(2L, "x");
        StandardResponse response = sendStandardRequest(UrlMapping.USER__SET_PASSWORD, setPasswordRequest);
        assertCinnamonError(response, ErrorCode.PASSWORD_TOO_SHORT);
    }

    @Test
    public void createUserHappyPath() throws IOException {
        String username = "new user";
        String password = "xxx12345";
        UserAccount user = new UserAccount(username, password, "A new user", "user@invalid.com",
                1L, LoginType.CINNAMON.name(), false, true, true);
        user.setGroupIds(List.of(1L));
        UserAccount userAccount = adminClient.createUser(user);
        assertEquals(username, userAccount.getName());
        assertEquals(user.getFullname(), userAccount.getFullname());
        assertEquals(user.getEmail(), userAccount.getEmail());
        assertEquals(user.getUiLanguageId(), userAccount.getUiLanguageId());
        assertEquals(user.getLoginType(), userAccount.getLoginType());
        assertEquals(user.isChangeTracking(), userAccount.isChangeTracking());
        assertEquals(user.isActivated(), userAccount.isActivated());
        assertEquals(user.isActivateTriggers(), userAccount.isActivateTriggers());

        var testClient = new CinnamonClient(adminClient.getPort(), adminClient.getHost(), adminClient.getProtocol(),
                username, password);
        testClient.connect();
        assertEquals(userAccount.getId(), testClient.getUser(userAccount.getId()).getId());
        assertEquals(userAccount.getGroupIds().get(0), 1L);
    }

    @Test
    public void createUserWithoutPassword() {
        String username = "new user without password";
        UserAccount user = new UserAccount(username, "", "A new user", "user@invalid.com",
                1L, LoginType.CINNAMON.name(), false, true, false);
        assertClientError(() -> adminClient.createUser(user), INVALID_REQUEST);
    }

    @Test
    public void createUserWithExistingName() {
        UserAccount user = new UserAccount("admin", "a second admin!", "A new user", "user@invalid.com",
                1L, LoginType.CINNAMON.name(), false, true, false);
        assertClientError(() -> adminClient.createUser(user), DB_INSERT_FAILED);
    }

    @Test
    public void createUserWithUnknownLoginType() {
        UserAccount user = new UserAccount("admin", "a second admin!", "A new user", "user@invalid.com",
                1L, "login via biometrically authenticated hug", false, true, false);
        assertClientError(() -> adminClient.createUser(user), LOGIN_TYPE_IS_UNKNOWN);
    }

    @Test
    public void createUserWithoutAdminAccess() {
        UserAccount user = new UserAccount("not-an-admin", "just-a-pass", "A new user", "user@invalid.com",
                1L, LoginType.CINNAMON.name(), false, true, true);
        assertClientError(() -> client.createUser(user), REQUIRES_SUPERUSER_STATUS);
    }

    @Test
    public void setPasswordOnNonCinnamonLoginType() throws IOException {
        UserAccount user = new UserAccount("user-with-other-login-type", "just-a-pass", "A new user", "user@invalid.com",
                1L, "LDAP", false, true, true);
        UserAccount userAccount = adminClient.createUser(user);
        assertClientError(() -> adminClient.setPassword(userAccount.getId(), "some other password"), USER_ACCOUNT_SET_PASSWORD_NOT_ALLOWED);
    }

    @Test
    public void setPassword() throws IOException {
        UserAccount user = new UserAccount("not-an-admin", "just-a-pass", "A new user", "user@invalid.com",
                1L, LoginType.CINNAMON.name(), false, true, true);
        UserAccount userAccount = adminClient.createUser(user);
        assertTrue(adminClient.setPassword(userAccount.getId(), "some other password"));
    }

    @Test
    public void createUserWithoutTooShortPassword() {
        String username = "new user with tiny password";
        UserAccount user = new UserAccount(username, "tiny", "A new user", "user@invalid.com",
                1L, LoginType.CINNAMON.name(), false, true, false);
        assertClientError(() -> adminClient.createUser(user), PASSWORD_TOO_SHORT);
    }

    @Test
    public void updateUser() throws IOException {
        String username = "update user";
        String password = "12345678";
        UserAccount user = new UserAccount(username, password, "A new user", "user@invalid.com",
                1L, LoginType.CINNAMON.name(), false, true, true);
        UserAccount account = adminClient.createUser(user);
        account.setName("updated user");
        UserAccount userAccount = adminClient.updateUser(account);
        assertEquals(account.getName(), userAccount.getName());
    }

    @Test
    public void updateUserGroups() throws IOException {
        // create a new user with a group which also has a permission.
        TestObjectHolder toh = new TestObjectHolder(adminClient, userId).createUser().createAcl().createGroup().createAclGroup();
        toh.addUserToGroup(toh.user.getId())
                .addPermissions(List.of(BROWSE));

        // update the user, so he is now only in another group
        UserAccount user = toh.user;
        user.setGroupIds(List.of(toh.createGroup().group.getId()));
        adminClient.updateUser(user);

        UserAccount updatedUser = client.getUser(user.getId());
        assertEquals(1, updatedUser.getGroupIds().size());
        assertEquals(toh.group.getId(), updatedUser.getGroupIds().get(0));
    }

    @Test
    public void updateWithDuplicateName() throws IOException {
        UserAccount user = new UserAccount("duplicate-name-test", "xxx123456", "A new user", "user@invalid.com",
                1L, LoginType.CINNAMON.name(), false, true, true);
        UserAccount account = adminClient.createUser(user);
        account.setName("admin");

        assertClientError(() -> adminClient.updateUser(account), DB_UPDATE_FAILED);
    }

    @Test
    public void updateWithoutChangingPassword() throws IOException {
        String password = "xxx123456";
        String username = "update-without-changing-password";
        String newUserName = "a new updated name";
        UserAccount user = new UserAccount(username, password, "A new user", "user@invalid.com",
                1L, LoginType.CINNAMON.name(), false, true, true);
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

    @Test
    public void setConfigInvalidRequest() {
        assertClientError(() -> client.setUserConfig(adminId, null), INVALID_REQUEST);
    }

    @Test
    public void setConfigOtherUserIsForbidden() {
        assertClientError(() -> client.setUserConfig(adminId, "xxx"), FORBIDDEN);
    }

    @Test
    public void setConfigUserNotFound() {
        assertClientError(() -> client.setUserConfig(Long.MAX_VALUE, "xxx"), USER_ACCOUNT_NOT_FOUND);
    }

    @Test
    public void setConfigSuperuserIsAllowed() throws IOException {
        adminClient.setUserConfig(userId, "<new-config/>");
        UserAccount user = client.getUser(userId);
        assertEquals("<new-config/>", user.getConfig());
    }

    @Test
    public void setConfigHappyPath() throws IOException {
        client.setUserConfig(userId, "<config>1</config>");
        UserAccount userWithNewConfig = client.getUser(userId);
        assertEquals("<config>1</config>", userWithNewConfig.getConfig());
    }

    @Test
    public void deleteUserHappyPath() throws IOException {
        TestObjectHolder toh = new TestObjectHolder(adminClient, adminId)
                .createGroup()
                .createAclGroup()
                .createUser();
        Long userId = toh.user.getId();
        toh.addUserToGroup(userId)
                .addPermissions(List.of(DefaultPermission.CREATE_OBJECT, DefaultPermission.CREATE_FOLDER, DefaultPermission.BROWSE, DefaultPermission.LOCK));

        CinnamonClient userClient = new CinnamonClient(client.getPort(), client.getHost(), client.getProtocol(), toh.user.getUsername(), toh.newUserPassword);
        userClient.connect();
        TestObjectHolder normalToh = new TestObjectHolder(userClient);
        normalToh.folder = creationFolder;
        normalToh.setUser(userId)
                .setAcl(toh.acl)
                .createFolder().createOsd().lockOsd();

        Long assetReceiverId = toh.createUser().user.getId();

        toh.deleteUser(userId, assetReceiverId);

        ObjectSystemData osd = client.getOsdById(normalToh.osd.getId(), false, false);
        assertEquals(adminId, osd.getModifierId());
        assertEquals(assetReceiverId, osd.getOwnerId());
        assertEquals(assetReceiverId, osd.getCreatorId());
        assertEquals(assetReceiverId, osd.getLockerId());
        Folder folder = client.getFolderById(normalToh.folder.getId(), false);
        assertEquals(assetReceiverId, folder.getOwnerId());
    }

    @Test
    public void deleteUserAsNormalUser() throws IOException {
        TestObjectHolder toh = new TestObjectHolder(adminClient, adminId)
                .createGroup()
                .createUser();
        Long userId = toh.user.getId();
        toh.addUserToGroup(userId);
        toh.createFolder().createOsd().lockOsd();
        Long assetReceiverId = toh.createUser().user.getId();
        assertClientError(() -> client.deleteUser(userId, assetReceiverId), ErrorCode.REQUIRES_SUPERUSER_STATUS);
    }

    @Test
    public void deleteSuperuser() {
        assertClientError(() -> adminClient.deleteUser(adminId, userId), ErrorCode.CANNOT_DELETE_SUPERUSER);
    }

    @Test
    public void deleteWithoutReceiver() {
        assertClientError(() -> adminClient.deleteUser(userId, 3289899L), ErrorCode.DELETE_USER_NEEDS_ASSET_RECEIVER);
    }

}
