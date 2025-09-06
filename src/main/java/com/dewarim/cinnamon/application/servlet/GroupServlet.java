package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.CinnamonResponse;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.dao.AclGroupDao;
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
import com.dewarim.cinnamon.model.response.DeleteResponse;
import com.dewarim.cinnamon.security.authorization.AccessFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;

import static com.dewarim.cinnamon.api.Constants.XML_MAPPER;

@WebServlet(name = "Group", urlPatterns = "/")
public class GroupServlet extends HttpServlet implements CruddyServlet<Group> {
    private static final Logger log = LogManager.getLogger(GroupServlet.class);

    private final ObjectMapper xmlMapper = XML_MAPPER;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        CinnamonResponse cinnamonResponse = (CinnamonResponse) response;
        GroupDao         groupDao         = new GroupDao();
        UserAccount      user             = ThreadLocalSqlSession.getCurrentUser();

        UrlMapping mapping = UrlMapping.getByPath(request.getRequestURI());
        switch (mapping) {
            case GROUP__ADD_USER_TO_GROUPS -> {
                superuserCheck();
                addUserToGroups(request, cinnamonResponse);
            }
            case GROUP__REMOVE_USER_FROM_GROUPS -> {
                removeUserFromGroups(request, cinnamonResponse);
                superuserCheck();
            }
            case GROUP__CREATE -> {
                superuserCheck();
                create(convertCreateRequest(request, CreateGroupRequest.class), groupDao, cinnamonResponse);
            }
            case GROUP__LIST -> list(convertListRequest(request, ListGroupRequest.class), groupDao, cinnamonResponse);
            case GROUP__DELETE -> {
                superuserCheck();
                DeleteGroupRequest deleteRequest = getMapper().readValue(request.getInputStream(), DeleteGroupRequest.class)
                        .validate().orElseThrow(ErrorCode.INVALID_REQUEST.getException());
                List<Long> groupIds = deleteRequest.list();
                for (Long groupId : groupIds) {
                    if (groupDao.hasChildren(groupId) && !deleteRequest.isRecursive()) {
                        log.debug("Group with id {} still has child groups.", groupId);
                        throw ErrorCode.GROUP_HAS_CHILDREN.exception();
                    }
                }
                deleteGroups(groupIds, groupDao, deleteRequest.isIgnoreNotFound());
                cinnamonResponse.setWrapper(new DeleteResponse(true));
                AccessFilter.reload();
            }
            case GROUP__UPDATE -> {
                superuserCheck();
                update(convertUpdateRequest(request, UpdateGroupRequest.class), groupDao, cinnamonResponse);
            }
            default -> ErrorCode.RESOURCE_NOT_FOUND.throwUp();
        }
    }

    private void deleteGroups(List<Long> ids, GroupDao groupDao, boolean ignoreNotFound) {
        List<Long> children = groupDao.getChildGroupIds(ids);
        if (!children.isEmpty()) {
            // TODO: if getChildGroupIds would recurse on the postgres side, we could skip recursion here (low prio)
            deleteGroups(children, groupDao, ignoreNotFound);
        }
        new AclGroupDao().deleteByGroupIds(ids);
        new GroupUserDao().deleteByGroupIds(ids);
        deleteByIds(ids, groupDao, ignoreNotFound);
    }

    private void removeUserFromGroups(HttpServletRequest request,CinnamonResponse cinnamonResponse) throws IOException {
        RemoveUserFromGroupsRequest removeRequest = getMapper().readValue(request.getInputStream(), RemoveUserFromGroupsRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());
        GroupUserDao groupUserDao = new GroupUserDao();
        groupUserDao.removeUserFromGroups(removeRequest.getUserId(), removeRequest.getGroupIds());
        AccessFilter.reloadUser(removeRequest.getUserId());
        cinnamonResponse.responseIsGenericOkay();
    }

    private void addUserToGroups(HttpServletRequest request, CinnamonResponse cinnamonResponse) throws IOException {
        AddUserToGroupsRequest addRequest = getMapper().readValue(request.getInputStream(), AddUserToGroupsRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());
        GroupUserDao groupUserDao = new GroupUserDao();
        groupUserDao.addUserToGroups(addRequest.getUserId(), addRequest.getGroupIds());
        AccessFilter.reloadUser(addRequest.getUserId());
        cinnamonResponse.responseIsGenericOkay();
    }

    @Override
    public ObjectMapper getMapper() {
        return xmlMapper;
    }
}