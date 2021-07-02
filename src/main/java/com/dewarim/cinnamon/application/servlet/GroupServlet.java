package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.CinnamonResponse;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.dao.GroupDao;
import com.dewarim.cinnamon.dao.GroupUserDao;
import com.dewarim.cinnamon.model.Group;
import com.dewarim.cinnamon.model.UserAccount;
import com.dewarim.cinnamon.model.request.group.CreateGroupRequest;
import com.dewarim.cinnamon.model.request.group.DeleteGroupRequest;
import com.dewarim.cinnamon.model.request.group.ListGroupRequest;
import com.dewarim.cinnamon.model.request.group.UpdateGroupRequest;
import com.dewarim.cinnamon.model.request.groupUser.AddUserToGroupsRequest;
import com.dewarim.cinnamon.model.request.groupUser.RemoveUserFromGroupsRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import static com.dewarim.cinnamon.api.Constants.XML_MAPPER;

@WebServlet(name = "Group", urlPatterns = "/")
public class GroupServlet extends HttpServlet implements CruddyServlet<Group> {

    private final ObjectMapper xmlMapper = XML_MAPPER;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        CinnamonResponse cinnamonResponse = (CinnamonResponse) response;
        GroupDao         groupDao         = new GroupDao();
        UserAccount      user             = ThreadLocalSqlSession.getCurrentUser();

        UrlMapping mapping = UrlMapping.getByPath(request.getRequestURI());
        switch (mapping) {
            case GROUP__ADD_USER_TO_GROUPS -> {
                superuserCheck();
                addUserToGroups(request, user, cinnamonResponse);
            }
            case GROUP__REMOVE_USER_FROM_GROUPS -> {
                removeUserFromGroups(request, user, cinnamonResponse);
                superuserCheck();
            }
            case GROUP__CREATE -> {
                superuserCheck();
                create(convertCreateRequest(request, CreateGroupRequest.class), groupDao, cinnamonResponse);
            }
            case GROUP__LIST -> list(convertListRequest(request, ListGroupRequest.class), groupDao, cinnamonResponse);
            case GROUP__DELETE -> {
                superuserCheck();
                delete(convertDeleteRequest(request, DeleteGroupRequest.class), groupDao, cinnamonResponse);
            }
            case GROUP__UPDATE -> {
                superuserCheck();
                update(convertUpdateRequest(request, UpdateGroupRequest.class), groupDao, cinnamonResponse);
            }
            default -> ErrorCode.RESOURCE_NOT_FOUND.throwUp();
        }
    }

    private void removeUserFromGroups(HttpServletRequest request, UserAccount user, CinnamonResponse cinnamonResponse) throws IOException {
        RemoveUserFromGroupsRequest removeRequest = getMapper().readValue(request.getReader(), RemoveUserFromGroupsRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());
        GroupUserDao groupUserDao = new GroupUserDao();
        groupUserDao.removeUserFromGroups(user.getId(), removeRequest.getIds());
        cinnamonResponse.responseIsGenericOkay();
    }

    private void addUserToGroups(HttpServletRequest request, UserAccount user, CinnamonResponse cinnamonResponse) throws IOException {
        AddUserToGroupsRequest addRequest = getMapper().readValue(request.getReader(), AddUserToGroupsRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());
        GroupUserDao groupUserDao = new GroupUserDao();
        groupUserDao.addUserToGroups(user.getId(), addRequest.getIds());
        cinnamonResponse.responseIsGenericOkay();
    }

    @Override
    public ObjectMapper getMapper() {
        return xmlMapper;
    }
}