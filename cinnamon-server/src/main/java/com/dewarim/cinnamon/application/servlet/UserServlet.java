package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.application.ErrorCode;
import com.dewarim.cinnamon.application.ErrorResponseGenerator;
import com.dewarim.cinnamon.dao.UserAccountDao;
import com.dewarim.cinnamon.model.UserAccount;
import com.dewarim.cinnamon.model.request.ListRequest;
import com.dewarim.cinnamon.model.request.UserInfoRequest;
import com.dewarim.cinnamon.model.response.UserInfo;
import com.dewarim.cinnamon.model.response.UserWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

import static com.dewarim.cinnamon.Constants.CONTENT_TYPE_XML;

/**
 */
@WebServlet(name = "User", urlPatterns = "/")
public class UserServlet extends HttpServlet {

    private ObjectMapper xmlMapper = new XmlMapper();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String pathInfo = request.getPathInfo();
        if(pathInfo == null){
            pathInfo = "";
        }
        switch (pathInfo) {
            case "/userInfo":
                showUserInfo(xmlMapper.readValue(request.getReader(), UserInfoRequest.class), response);
                break;
            case "/listUsers":
                listUsers(request,response);
                break;
            default:
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }

    }
    
    private void listUsers(HttpServletRequest request, HttpServletResponse response) throws IOException{
        // ignore listRequest for now, just make sure it's valid xml:
        ListRequest listRequest = xmlMapper.readValue(request.getInputStream(), ListRequest.class);

        UserAccountDao userAccountDao = new UserAccountDao();
        UserWrapper wrapper = new UserWrapper();
        wrapper.setUsers(userAccountDao.listUserAccountsAsUserInfo());
        response.setContentType(CONTENT_TYPE_XML);
        response.setStatus(HttpServletResponse.SC_OK);
        xmlMapper.writeValue(response.getWriter(), wrapper);
    }

    private void showUserInfo(UserInfoRequest userInfoRequest, HttpServletResponse response) throws IOException {
        UserAccountDao userAccountDao = new UserAccountDao();
        UserAccount user;
        if (userInfoRequest.byId()) {
            user = userAccountDao.getUserAccountById(userInfoRequest.getUserId());
        }
        else if (userInfoRequest.byName()) {
            user = userAccountDao.getUserAccountByName(userInfoRequest.getUsername());
        }
        else {
            ErrorResponseGenerator.generateErrorMessage(response, HttpServletResponse.SC_BAD_REQUEST, ErrorCode.USER_INFO_REQUEST_WITHOUT_NAME_OR_ID, "Request needs id or username to be set.");
            return;
        }
        if(user == null){
            ErrorResponseGenerator.generateErrorMessage(response, HttpServletResponse.SC_BAD_REQUEST, ErrorCode.USER_ACCOUNT_NOT_FOUND, "Could not find user.");
            return;
        }
        UserInfo userInfo = new UserInfo(user.getId(), user.getName(), user.getLoginType());
        UserWrapper wrapper = new UserWrapper();
        wrapper.setUsers(Collections.singletonList(userInfo));
        response.setContentType(CONTENT_TYPE_XML);
        response.setStatus(HttpServletResponse.SC_OK);
        xmlMapper.writeValue(response.getWriter(), wrapper);
    }
}
