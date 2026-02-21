package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.CinnamonRequest;
import com.dewarim.cinnamon.application.CinnamonResponse;
import com.dewarim.cinnamon.application.RequestScope;
import com.dewarim.cinnamon.configuration.CinnamonConfig;
import com.dewarim.cinnamon.configuration.SecurityConfig;
import com.dewarim.cinnamon.dao.FolderDao;
import com.dewarim.cinnamon.dao.GroupUserDao;
import com.dewarim.cinnamon.dao.OsdDao;
import com.dewarim.cinnamon.dao.UserAccountDao;
import com.dewarim.cinnamon.model.GroupUser;
import com.dewarim.cinnamon.model.LoginType;
import com.dewarim.cinnamon.model.UserAccount;
import com.dewarim.cinnamon.model.request.CreateRequest;
import com.dewarim.cinnamon.model.request.UpdateRequest;
import com.dewarim.cinnamon.model.request.user.*;
import com.dewarim.cinnamon.model.response.UserAccountWrapper;
import com.dewarim.cinnamon.security.HashMaker;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static com.dewarim.cinnamon.api.Constants.CINNAMON_CONFIG;

/**
 *
 */
@WebServlet(name = "UserAccount", urlPatterns = "/")
public class UserAccountServlet extends HttpServlet implements CruddyServlet<UserAccount> {

    private static final Logger         log = LogManager.getLogger(UserAccountServlet.class);
    private              SecurityConfig securityConfig;

    public UserAccountServlet() {
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        CinnamonResponse cinnamonResponse = (CinnamonResponse) response;
        CinnamonRequest  cinnamonRequest  = (CinnamonRequest) request;
        UserAccountDao   userAccountDao   = new UserAccountDao();
        GroupUserDao     groupUserDao     = new GroupUserDao();

        UrlMapping mapping = UrlMapping.getByPath(request.getRequestURI());
        switch (mapping) {
            case USER__GET ->
                    showUserInfo(cinnamonRequest.getMapper().readValue(request.getInputStream(), GetUserAccountRequest.class), userAccountDao,
                            groupUserDao, cinnamonResponse);
            case USER__LIST -> {
                list(convertListRequest(cinnamonRequest, ListUserAccountRequest.class), userAccountDao, cinnamonResponse);
                ((List<UserAccount>) cinnamonResponse.getWrapper().list()).forEach(user -> {
                    user.filterInfo();
                    addUserGroupIds(user, groupUserDao);
                });
            }
            case USER__CREATE -> {
                superuserCheck();
                CreateRequest<UserAccount> createRequest = convertCreateRequest(cinnamonRequest, CreateUserAccountRequest.class);
                createRequest.list().forEach(userAccount -> {
                    if (!LoginType.isKnown(userAccount.getLoginType())) {
                        throw ErrorCode.LOGIN_TYPE_IS_UNKNOWN.exception();
                    }
                    if (passwordIsTooShort(userAccount.getPassword())) {
                        throw ErrorCode.PASSWORD_TOO_SHORT.exception();
                    }
                    userAccount.setPassword(HashMaker.createDigest(userAccount.getPassword()));
                });
                create(createRequest, userAccountDao, cinnamonResponse);
                ((List<UserAccount>) cinnamonResponse.getWrapper().list()).forEach(user -> {
                    user.filterInfo();
                    groupUserDao.addUserToGroups(user.getId(), user.getGroupIds());
                });
            }
            case USER__SET_PASSWORD -> setPassword(cinnamonRequest, userAccountDao, cinnamonResponse);
            case USER__UPDATE -> {
                superuserCheck();
                UpdateRequest<UserAccount> updateRequest = convertUpdateRequest(cinnamonRequest, UpdateUserAccountRequest.class);
                updateRequest.list().forEach(userAccount -> {
                            Long userId = userAccount.getId();
                            if (userAccount.getPassword() != null && userAccount.getPassword().trim().length() > 0) {
                                // user has supplied a new password:
                                userAccount.setPassword(HashMaker.createDigest(userAccount.getPassword()));
                            } else {
                                // keep the old password: load encrypted password for this user.
                                UserAccount accountFromDb = userAccountDao.getUserAccountById(userId)
                                        .orElseThrow(ErrorCode.USER_ACCOUNT_NOT_FOUND.getException());
                                userAccount.setPassword(accountFromDb.getPassword());
                            }
                            // update the user's groups:
                            groupUserDao.removeUserFromGroups(userId, groupUserDao.listGroupsOfUser(userId).stream().map(GroupUser::getGroupId).toList());
                            groupUserDao.addUserToGroups(userId, userAccount.getGroupIds());
                        }
                );
                update(updateRequest, userAccountDao, cinnamonResponse);
                ((List<UserAccount>) cinnamonResponse.getWrapper().list()).forEach(user -> {
                    user.filterInfo();
                    addUserGroupIds(user, groupUserDao);
                });
            }
            case USER__SET_CONFIG -> {
                SetUserConfigRequest configRequest = cinnamonRequest.getMapper().readValue(request.getInputStream(), SetUserConfigRequest.class)
                        .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());
                UserAccount           currentUser = RequestScope.getCurrentUser();
                Optional<UserAccount> userOpt     = userAccountDao.getUserAccountById(configRequest.getUserId());
                if (userOpt.isEmpty()) {
                    throw ErrorCode.USER_ACCOUNT_NOT_FOUND.exception();
                }
                if (!configRequest.getUserId().equals(currentUser.getId())) {
                    if (!userAccountDao.isSuperuser(currentUser)) {
                        throw ErrorCode.FORBIDDEN.exception();
                    }
                }
                UserAccount user = userOpt.get();
                user.setConfig(configRequest.getConfig());
                userAccountDao.updateUser(user);
                cinnamonResponse.responseIsGenericOkay();
            }
            case USER__DELETE -> {
                superuserCheck();
                DeleteUserAccountRequest deleteUserAccountRequest = cinnamonRequest.getMapper().readValue(request.getInputStream(), DeleteUserAccountRequest.class)
                        .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());
                Long assetReceiverId = deleteUserAccountRequest.getAssetReceiverId();
                userAccountDao.getUserAccountById(assetReceiverId).orElseThrow(ErrorCode.DELETE_USER_NEEDS_ASSET_RECEIVER::exception);
                Long        userId      = deleteUserAccountRequest.getUserId();
                UserAccount userAccount = userAccountDao.getUserAccountById(userId).orElseThrow(ErrorCode.USER_ACCOUNT_NOT_FOUND::exception);
                if (userAccountDao.isSuperuser(userAccount)) {
                    throw ErrorCode.CANNOT_DELETE_SUPERUSER.exception();
                }
                UserAccount currentUser = RequestScope.getCurrentUser();

                new FolderDao().updateOwnership(userId, assetReceiverId);
                new OsdDao().updateOwnershipAndModifierAndCreatorAndLocker(userId, assetReceiverId, currentUser.getId());
                new GroupUserDao().deleteByUserId(userId);

                userAccountDao.delete(List.of(userId));
                cinnamonResponse.responseIsGenericOkay();
            }
            default -> ErrorCode.RESOURCE_NOT_FOUND.throwUp();
        }
    }

    private void addUserGroupIds(UserAccount userAccount, GroupUserDao groupUserDao) {
        List<Long> groupIds = groupUserDao.listGroupsOfUser(userAccount.getId()).stream().map(GroupUser::getGroupId).toList();
        userAccount.setGroupIds(groupIds);
    }

    private boolean passwordIsTooShort(String password) {
        return password == null || password.trim().length() < securityConfig.getMinimumPasswordLength();
    }

    private void setPassword(CinnamonRequest request, UserAccountDao userDao, CinnamonResponse response) throws IOException {
        SetPasswordRequest passwordRequest = request.getMapper().readValue(request.getInputStream(), SetPasswordRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());
        UserAccount           currentUser = RequestScope.getCurrentUser();
        Optional<UserAccount> userOpt     = userDao.getUserAccountById(passwordRequest.getUserId());

        if (!passwordRequest.getUserId().equals(currentUser.getId())) {
            if (!userDao.isSuperuser(currentUser)) {
                throw ErrorCode.FORBIDDEN.exception();
            }
        }

        if (passwordIsTooShort(passwordRequest.getPassword())) {
            throw ErrorCode.PASSWORD_TOO_SHORT.exception();
        }

        UserAccount user = userOpt.orElseThrow(ErrorCode.USER_ACCOUNT_NOT_FOUND.getException());
        if (!user.getLoginType().equals(LoginType.CINNAMON.name())) {
            throw ErrorCode.USER_ACCOUNT_SET_PASSWORD_NOT_ALLOWED.exception();
        }
        String pwdHash = HashMaker.createDigest(passwordRequest.getPassword());
        user.setPassword(pwdHash);
        userDao.updateUser(user);
        response.responseIsGenericOkay();
    }

    private void showUserInfo(GetUserAccountRequest userInfoRequest, UserAccountDao userAccountDao, GroupUserDao groupUserDao, CinnamonResponse response) {
        Optional<UserAccount> userOpt;
        if (userInfoRequest.byId()) {
            userOpt = userAccountDao.getUserAccountById(userInfoRequest.getUserId());
        } else if (userInfoRequest.byName()) {
            userOpt = userAccountDao.getUserAccountByName(userInfoRequest.getUsername());
        } else {
            throw ErrorCode.USER_INFO_REQUEST_WITHOUT_NAME_OR_ID.exception();
        }

        UserAccount user = userOpt.orElseThrow(ErrorCode.USER_ACCOUNT_NOT_FOUND.getException());
        user.filterInfo();
        addUserGroupIds(user, groupUserDao);
        UserAccountWrapper wrapper = new UserAccountWrapper(List.of(user));
        response.setWrapper(wrapper);
    }

    @Override
    public void init() {
        securityConfig = ((CinnamonConfig) getServletContext().getAttribute(CINNAMON_CONFIG)).getSecurityConfig();
    }
}
