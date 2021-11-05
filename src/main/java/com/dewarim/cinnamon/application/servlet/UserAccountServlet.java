package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.CinnamonResponse;
import com.dewarim.cinnamon.application.CinnamonServer;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.configuration.SecurityConfig;
import com.dewarim.cinnamon.dao.UserAccountDao;
import com.dewarim.cinnamon.model.UserAccount;
import com.dewarim.cinnamon.model.request.CreateRequest;
import com.dewarim.cinnamon.model.request.UpdateRequest;
import com.dewarim.cinnamon.model.request.user.CreateUserAccountRequest;
import com.dewarim.cinnamon.model.request.user.GetUserAccountRequest;
import com.dewarim.cinnamon.model.request.user.ListUserAccountRequest;
import com.dewarim.cinnamon.model.request.user.SetPasswordRequest;
import com.dewarim.cinnamon.model.request.user.UpdateUserAccountRequest;
import com.dewarim.cinnamon.model.response.UserAccountWrapper;
import com.dewarim.cinnamon.security.HashMaker;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.dewarim.cinnamon.api.Constants.XML_MAPPER;
import static com.dewarim.cinnamon.application.ResponseUtil.responseIsOkayAndXml;

/**
 *
 */
@WebServlet(name = "UserAccount", urlPatterns = "/")
public class UserAccountServlet extends HttpServlet implements CruddyServlet<UserAccount> {

    private static final Logger       log       = LogManager.getLogger(UserAccountServlet.class);
    private final        ObjectMapper xmlMapper = XML_MAPPER;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        CinnamonResponse cinnamonResponse = (CinnamonResponse) response;
        UserAccountDao   userAccountDao   = new UserAccountDao();

        UrlMapping mapping = UrlMapping.getByPath(request.getRequestURI());
        switch (mapping) {
            case USER__GET -> showUserInfo(xmlMapper.readValue(request.getReader(), GetUserAccountRequest.class), userAccountDao, cinnamonResponse);
            case USER__LIST -> {
                list(convertListRequest(request, ListUserAccountRequest.class), userAccountDao, cinnamonResponse);
                ((List<UserAccount>) cinnamonResponse.getWrapper().list()).forEach(UserAccount::filterInfo);
            }
            case USER__CREATE -> {
                superuserCheck();
                CreateRequest<UserAccount> createRequest = convertCreateRequest(request, CreateUserAccountRequest.class);
                createRequest.list().forEach(userAccount -> {
                    if (passwordIsTooShort(userAccount.getPassword())) {
                        ErrorCode.PASSWORD_TOO_SHORT.throwUp();
                    }
                    userAccount.setPassword(HashMaker.createDigest(userAccount.getPassword()));
                });
                create(createRequest, userAccountDao, cinnamonResponse);
                ((List<UserAccount>) cinnamonResponse.getWrapper().list()).forEach(UserAccount::filterInfo);
            }
            case USER__SET_PASSWORD -> setPassword(request, userAccountDao, cinnamonResponse);
            case USER__UPDATE -> {
                superuserCheck();
                UpdateRequest<UserAccount> updateRequest = convertUpdateRequest(request, UpdateUserAccountRequest.class);
                updateRequest.list().forEach(userAccount -> {
                            if (userAccount.getPassword() != null && userAccount.getPassword().trim().length() > 0) {
                                // user has supplied a new password:
                                userAccount.setPassword(HashMaker.createDigest(userAccount.getPassword()));
                            } else {
                                // keep the old password: load encrypted password for this user.
                                UserAccount accountFromDb = userAccountDao.getUserAccountById(userAccount.getId())
                                        .orElseThrow(ErrorCode.USER_ACCOUNT_NOT_FOUND.getException());
                                userAccount.setPassword(accountFromDb.getPassword());
                            }
                        }
                );
                update(updateRequest, userAccountDao, cinnamonResponse);
                ((List<UserAccount>) cinnamonResponse.getWrapper().list()).forEach(UserAccount::filterInfo);
            }
            default -> ErrorCode.RESOURCE_NOT_FOUND.throwUp();
        }
    }

    private boolean passwordIsTooShort(String password) {
        SecurityConfig config = CinnamonServer.config.getSecurityConfig();
        return password == null || password.trim().length() < config.getMinimumPasswordLength();
    }

    private void setPassword(HttpServletRequest request, UserAccountDao userDao, CinnamonResponse response) throws IOException {
        SetPasswordRequest    passwordRequest = xmlMapper.readValue(request.getInputStream(), SetPasswordRequest.class);
        UserAccount           currentUser     = ThreadLocalSqlSession.getCurrentUser();
        Optional<UserAccount> userOpt         = userDao.getUserAccountById(passwordRequest.getUserId());

        if (!passwordRequest.getUserId().equals(currentUser.getId())) {
            if (!userDao.isSuperuser(currentUser)) {
                ErrorCode.FORBIDDEN.throwUp();
            }
        }

        if (passwordIsTooShort(passwordRequest.getPassword())) {
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

    private void showUserInfo(GetUserAccountRequest userInfoRequest, UserAccountDao userAccountDao, CinnamonResponse response) {
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
        user.filterInfo();
        UserAccountWrapper wrapper = new UserAccountWrapper();
        wrapper.setList(Collections.singletonList(user));
        response.setWrapper(wrapper);
    }

    @Override
    public ObjectMapper getMapper() {
        return xmlMapper;
    }
}
