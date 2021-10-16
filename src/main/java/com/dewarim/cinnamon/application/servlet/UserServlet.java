package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.CinnamonResponse;
import com.dewarim.cinnamon.application.CinnamonServer;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.configuration.SecurityConfig;
import com.dewarim.cinnamon.dao.UserAccountDao;
import com.dewarim.cinnamon.model.UserAccount;
import com.dewarim.cinnamon.model.request.DefaultListRequest;
import com.dewarim.cinnamon.model.request.user.ListUserInfoRequest;
import com.dewarim.cinnamon.model.request.user.SetPasswordRequest;
import com.dewarim.cinnamon.model.request.user.UserInfoRequest;
import com.dewarim.cinnamon.model.response.UserInfo;
import com.dewarim.cinnamon.model.response.UserWrapper;
import com.dewarim.cinnamon.security.HashMaker;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

import static com.dewarim.cinnamon.api.Constants.XML_MAPPER;
import static com.dewarim.cinnamon.application.ResponseUtil.responseIsOkayAndXml;

/**
 *
 */
@WebServlet(name = "User", urlPatterns = "/")
public class UserServlet extends HttpServlet {

    private final ObjectMapper xmlMapper = XML_MAPPER;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        CinnamonResponse cinnamonResponse = (CinnamonResponse) response;
        UserAccountDao        userAccountDao = new UserAccountDao();

        UrlMapping mapping = UrlMapping.getByPath(request.getRequestURI());
        switch (mapping) {
            case USER__USER_INFO -> showUserInfo(xmlMapper.readValue(request.getReader(), UserInfoRequest.class), userAccountDao, cinnamonResponse);
            case USER__LIST_USERS -> listUsers(request, userAccountDao, cinnamonResponse);
            case USER__SET_PASSWORD -> setPassword(request, userAccountDao, cinnamonResponse);
            default -> ErrorCode.RESOURCE_NOT_FOUND.throwUp();
        }
    }

    private void setPassword(HttpServletRequest request, UserAccountDao userDao,CinnamonResponse response) throws IOException {
        SetPasswordRequest    passwordRequest = xmlMapper.readValue(request.getInputStream(), SetPasswordRequest.class);
        UserAccount           currentUser     = ThreadLocalSqlSession.getCurrentUser();
        SecurityConfig        config          = CinnamonServer.config.getSecurityConfig();
        Optional<UserAccount> userOpt         = userDao.getUserAccountById(passwordRequest.getUserId());

        if (!passwordRequest.getUserId().equals(currentUser.getId())) {
            if (!userDao.isSuperuser(currentUser)) {
                ErrorCode.FORBIDDEN.throwUp();
            }
        }

        if (passwordRequest.getPassword() == null || passwordRequest.getPassword().length() < config.getMinimumPasswordLength()) {
            ErrorCode.PASSWORD_TOO_SHORT.throwUp();
        }

        if (userOpt.isEmpty()) {
            ErrorCode.USER_ACCOUNT_NOT_FOUND.throwUp();
        }

        String      pwdHash = HashMaker.createDigest(passwordRequest.getPassword());
        UserAccount user    = userOpt.get();
        user.setPassword(pwdHash);
        userDao.updateUser(user);
        responseIsOkayAndXml(response);
    }

    private void listUsers(HttpServletRequest request,UserAccountDao userAccountDao, CinnamonResponse response) throws IOException {
        // ignore listRequest for now, just make sure it's valid xml:
        DefaultListRequest listRequest    = xmlMapper.readValue(request.getInputStream(), ListUserInfoRequest.class);
        UserWrapper        wrapper        = new UserWrapper();
        wrapper.setUsers(userAccountDao.listUserAccountsAsUserInfo());
        response.setWrapper(wrapper);
    }

    private void showUserInfo(UserInfoRequest userInfoRequest,UserAccountDao userAccountDao, CinnamonResponse response) {
        Optional<UserAccount> userOpt;
        if (userInfoRequest.byId()) {
            userOpt = userAccountDao.getUserAccountById(userInfoRequest.getUserId());
        } else if (userInfoRequest.byName()) {
            userOpt = userAccountDao.getUserAccountByName(userInfoRequest.getUsername());
        } else {
            ErrorCode.USER_INFO_REQUEST_WITHOUT_NAME_OR_ID.throwUp();
            return;
        }
        if (userOpt.isEmpty()) {
            ErrorCode.USER_ACCOUNT_NOT_FOUND.throwUp();
        }

        UserAccount user = userOpt.get();
        UserInfo userInfo = new UserInfo(user.getId(), user.getName(), user.getLoginType(),
                user.isActivated(), user.isLocked(), user.getUiLanguageId(), user.getEmail(), user.getFullname(), user.isChangeTracking(),
                user.isPasswordExpired(), user.getGroupIds());
        UserWrapper wrapper = new UserWrapper();
        wrapper.setUsers(Collections.singletonList(userInfo));
        response.setWrapper(wrapper);
    }
}
