package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.FailedRequestException;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.CinnamonResponse;
import com.dewarim.cinnamon.dao.AclGroupDao;
import com.dewarim.cinnamon.dao.AclGroupPermissionDao;
import com.dewarim.cinnamon.model.AclGroup;
import com.dewarim.cinnamon.model.request.CreateRequest;
import com.dewarim.cinnamon.model.request.DeleteRequest;
import com.dewarim.cinnamon.model.request.UpdateRequest;
import com.dewarim.cinnamon.model.request.aclGroup.*;
import com.dewarim.cinnamon.model.response.AclGroupWrapper;
import com.dewarim.cinnamon.security.authorization.AccessFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.dewarim.cinnamon.api.Constants.XML_MAPPER;

@WebServlet(name = "AclGroup", urlPatterns = "/")
public class AclGroupServlet extends HttpServlet implements CruddyServlet<AclGroup> {

    private final ObjectMapper xmlMapper = XML_MAPPER;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        CinnamonResponse cinnamonResponse = (CinnamonResponse) response;
        AclGroupDao      aclGroupDao      = new AclGroupDao();

        UrlMapping mapping = UrlMapping.getByPath(request.getRequestURI());
        switch (mapping) {
            case ACL_GROUP__LIST_BY_GROUP_OR_ACL -> listAclGroups(request, cinnamonResponse, aclGroupDao);
            case ACL_GROUP__LIST -> {
                list(convertListRequest(request, ListAclGroupRequest.class), aclGroupDao, cinnamonResponse);
                aclGroupDao.loadPermissionsIntoAclGroups(((AclGroupWrapper) cinnamonResponse.getWrapper()).list());
            }
            case ACL_GROUP__CREATE -> {
                superuserCheck();
                CreateRequest<AclGroup> createRequest = convertCreateRequest(request, CreateAclGroupRequest.class);
                List<AclGroup> aclGroupsToCreate = createRequest.list();
                List<AclGroup> aclGroups         = aclGroupDao.create(aclGroupsToCreate);
                AclGroupPermissionDao aclGroupPermissionDao = new AclGroupPermissionDao();
                for (AclGroup aclGroup : aclGroupsToCreate) {
                    if (aclGroup.getPermissionIds().size() > 0) {
                        Optional<AclGroup> newGroup = aclGroups.stream().filter(ag -> ag.getAclId().equals(aclGroup.getAclId()) && ag.getGroupId().equals(aclGroup.getGroupId()))
                                .findFirst();
                        // if we cannot create all the aclGroups, create() should throw an DB_INSERT_FAILED error.
                        // therefore we do not handle missing group here.
                        newGroup.ifPresent(group -> aclGroupPermissionDao.addPermissions(group, aclGroup.getPermissionIds()));
                    }
                }
                cinnamonResponse.setWrapper(createRequest.fetchResponseWrapper().setList(aclGroups));
            }
            case ACL_GROUP__DELETE -> {
                superuserCheck();
                DeleteRequest<AclGroup> aclGroupDeleteRequest = convertDeleteRequest(request, DeleteAclGroupRequest.class);
                List<AclGroup>          groupsToDelete        = aclGroupDao.getObjectsById(aclGroupDeleteRequest.list());
                aclGroupDao.loadPermissionsIntoAclGroups(groupsToDelete);
                AclGroupPermissionDao permissionDao = new AclGroupPermissionDao();
                groupsToDelete.forEach(group -> permissionDao.removePermissions(group, group.getPermissionIds()));
                delete(aclGroupDeleteRequest, aclGroupDao, cinnamonResponse);
            }
            case ACL_GROUP__UPDATE -> {
                superuserCheck();
                UpdateRequest<AclGroup> updateRequest       = convertUpdateRequest(request, UpdateAclGroupRequest.class);
                List<Long>              aclGroupIds         = updateRequest.list().stream().map(AclGroup::getId).toList();
                AclGroupPermissionDao   permissionDao       = new AclGroupPermissionDao();
                List<AclGroup>          currentGroupsFromDb = aclGroupDao.getObjectsById(aclGroupIds);
                aclGroupDao.loadPermissionsIntoAclGroups(currentGroupsFromDb);
                Map<Long, AclGroup> requestGroups = new HashMap<>();
                updateRequest.list().forEach(ag -> requestGroups.put(ag.getId(), ag));
                for (AclGroup currentGroup : currentGroupsFromDb) {
                    permissionDao.removePermissions(currentGroup, currentGroup.getPermissionIds());
                    AclGroup requestGroup = requestGroups.get(currentGroup.getId());
                    permissionDao.addPermissions(currentGroup, requestGroup.getPermissionIds());
                }
                AclGroupWrapper aclGroupWrapper = new AclGroupWrapper(updateRequest.list());
                cinnamonResponse.setWrapper(aclGroupWrapper);
                AccessFilter.reload();
            }
            default -> ErrorCode.RESOURCE_NOT_FOUND.throwUp();
        }
    }

    private void listAclGroups(HttpServletRequest request, CinnamonResponse response, AclGroupDao aclGroupDao) throws IOException {
        AclGroupListRequest listRequest = xmlMapper.readValue(request.getInputStream(), AclGroupListRequest.class)
                .validateRequest().orElseThrow(() -> new FailedRequestException(ErrorCode.INVALID_REQUEST));

        List<AclGroup> entries = switch (listRequest.getIdType()) {
            case ACL -> aclGroupDao.getAclGroupsByAclId(listRequest.getId());
            case GROUP -> aclGroupDao.getAclGroupsByGroupId(listRequest.getId());
        };
        response.setWrapper(new AclGroupWrapper(entries));
    }

    @Override
    public ObjectMapper getMapper() {
        return xmlMapper;
    }
}