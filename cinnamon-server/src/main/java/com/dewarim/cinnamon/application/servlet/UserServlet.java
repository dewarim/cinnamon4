package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.application.CinnamonServer;
import com.dewarim.cinnamon.application.ErrorCode;
import com.dewarim.cinnamon.application.ErrorResponseGenerator;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.configuration.SecurityConfig;
import com.dewarim.cinnamon.dao.UserAccountDao;
import com.dewarim.cinnamon.model.UserAccount;
import com.dewarim.cinnamon.model.request.ListRequest;
import com.dewarim.cinnamon.model.request.user.SetPasswordRequest;
import com.dewarim.cinnamon.model.request.user.UserInfoRequest;
import com.dewarim.cinnamon.model.response.GenericResponse;
import com.dewarim.cinnamon.model.response.UserInfo;
import com.dewarim.cinnamon.model.response.UserWrapper;
import com.dewarim.cinnamon.security.HashMaker;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

import static com.dewarim.cinnamon.Constants.CONTENT_TYPE_XML;
import static com.dewarim.cinnamon.application.ResponseUtil.responseIsOkayAndXml;
import static jakarta.servlet.http.HttpServletResponse.*;

/**
 */
@WebServlet(name = "User", urlPatterns = "/")
public class UserServlet extends HttpServlet {

    private ObjectMapper xmlMapper = new XmlMapper();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            pathInfo = "";
        }
        switch (pathInfo) {
            case "/userInfo":
                showUserInfo(xmlMapper.readValue(request.getReader(), UserInfoRequest.class), response);
                break;
            case "/listUsers":
                listUsers(request, response);
                break;
            case "/setPassword":
                setPassword(request, response);
                break;
            default:
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }

    }

    private void setPassword(HttpServletRequest request, HttpServletResponse response) throws IOException {
        SetPasswordRequest    passwordRequest = xmlMapper.readValue(request.getInputStream(), SetPasswordRequest.class);
        UserAccountDao        userDao         = new UserAccountDao();
        UserAccount           currentUser     = ThreadLocalSqlSession.getCurrentUser();
        SecurityConfig        config          = CinnamonServer.config.getSecurityConfig();
        Optional<UserAccount> userOpt         = userDao.getUserAccountById(passwordRequest.getUserId());

        if (!passwordRequest.getUserId().equals(currentUser.getId())) {

            if (!userDao.isSuperuser(currentUser)) {
                ErrorResponseGenerator.generateErrorMessage(response, SC_FORBIDDEN, ErrorCode.FORBIDDEN);
                return;
            }
        }

        if (passwordRequest.getPassword() == null || passwordRequest.getPassword().length() < config.getMinimumPasswordLength()) {
            ErrorResponseGenerator.generateErrorMessage(response, SC_BAD_REQUEST, ErrorCode.PASSWORD_TOO_SHORT);
            return;
        }

        if (!userOpt.isPresent()) {
            ErrorResponseGenerator.generateErrorMessage(response, SC_NOT_FOUND, ErrorCode.USER_ACCOUNT_NOT_FOUND);
            return;
        }

        String      pwdHash = HashMaker.createDigest(passwordRequest.getPassword());
        UserAccount user    = userOpt.get();
        user.setPassword(pwdHash);
        userDao.updateUser(user);
        responseIsOkayAndXml(response);
        xmlMapper.writeValue(response.getWriter(), new GenericResponse(true));
    }

    private void listUsers(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // ignore listRequest for now, just make sure it's valid xml:
        ListRequest listRequest = xmlMapper.readValue(request.getInputStream(), ListRequest.class);

        UserAccountDao userAccountDao = new UserAccountDao();
        UserWrapper    wrapper        = new UserWrapper();
        wrapper.setUsers(userAccountDao.listUserAccountsAsUserInfo());
        response.setContentType(CONTENT_TYPE_XML);
        response.setStatus(HttpServletResponse.SC_OK);
        xmlMapper.writeValue(response.getWriter(), wrapper);
    }

    private void showUserInfo(UserInfoRequest userInfoRequest, HttpServletResponse response) throws IOException {
        UserAccountDao        userAccountDao = new UserAccountDao();
        Optional<UserAccount> userOpt;
        if (userInfoRequest.byId()) {
            userOpt = userAccountDao.getUserAccountById(userInfoRequest.getUserId());
        }
        else if (userInfoRequest.byName()) {
            userOpt = userAccountDao.getUserAccountByName(userInfoRequest.getUsername());
        }
        else {
            ErrorResponseGenerator.generateErrorMessage(response, ErrorCode.USER_INFO_REQUEST_WITHOUT_NAME_OR_ID);
            return;
        }
        if (!userOpt.isPresent()) {
            ErrorResponseGenerator.generateErrorMessage(response, ErrorCode.USER_ACCOUNT_NOT_FOUND);
            return;
        }

        UserAccount user = userOpt.get();
        UserInfo userInfo = new UserInfo(user.getId(), user.getName(), user.getLoginType(),
                user.isActivated(), user.isLocked(), user.getUiLanguageId(), user.getEmail(), user.getFullname(), user.isChangeTracking());
        UserWrapper wrapper = new UserWrapper();
        wrapper.setUsers(Collections.singletonList(userInfo));
        response.setContentType(CONTENT_TYPE_XML);
        response.setStatus(HttpServletResponse.SC_OK);
        xmlMapper.writeValue(response.getWriter(), wrapper);
    }
}
