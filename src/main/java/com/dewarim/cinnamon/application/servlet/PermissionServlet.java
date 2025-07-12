package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.CinnamonResponse;
import com.dewarim.cinnamon.application.ErrorResponseGenerator;
import com.dewarim.cinnamon.dao.AclDao;
import com.dewarim.cinnamon.dao.AclGroupDao;
import com.dewarim.cinnamon.dao.AclGroupPermissionDao;
import com.dewarim.cinnamon.dao.PermissionDao;
import com.dewarim.cinnamon.model.Acl;
import com.dewarim.cinnamon.model.AclGroup;
import com.dewarim.cinnamon.model.Permission;
import com.dewarim.cinnamon.model.request.permission.ChangePermissionsRequest;
import com.dewarim.cinnamon.model.request.permission.ListPermissionRequest;
import com.dewarim.cinnamon.model.request.user.UserPermissionRequest;
import com.dewarim.cinnamon.model.response.PermissionWrapper;
import com.dewarim.cinnamon.security.authorization.AccessFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static com.dewarim.cinnamon.api.Constants.XML_MAPPER;

/**
 *
 */
@WebServlet(name = "Permission", urlPatterns = "/")
public class PermissionServlet extends HttpServlet implements CruddyServlet<Permission> {

    private final ObjectMapper xmlMapper = XML_MAPPER;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        CinnamonResponse cinnamonResponse = (CinnamonResponse) response;
        PermissionDao    permissionDao    = new PermissionDao();

        UrlMapping mapping = UrlMapping.getByPath(request.getRequestURI());
        switch (mapping) {
            case PERMISSION__LIST -> list(convertListRequest(request, ListPermissionRequest.class), permissionDao, cinnamonResponse);
            case PERMISSION__GET_USER_PERMISSIONS -> getUserPermissions(request, cinnamonResponse);
            case PERMISSION__CHANGE_PERMISSIONS -> {
                superuserCheck();
                changePermissions(request, cinnamonResponse);
            }
            default -> ErrorCode.RESOURCE_NOT_FOUND.throwUp();
        }
    }

    private void changePermissions(HttpServletRequest request, CinnamonResponse cinnamonResponse) throws IOException {
        ChangePermissionsRequest changeRequest = getMapper().readValue(request.getInputStream(), ChangePermissionsRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());
        var            aclGroupDao = new AclGroupDao();
        List<AclGroup> aclGroups   = aclGroupDao.getObjectsById(List.of(changeRequest.getAclGroupId()));
        if (aclGroups.isEmpty()) {
            ErrorCode.ACL_GROUP_NOT_FOUND.throwUp();
        }
        var aclGroup              = aclGroups.getFirst();
        var aclGroupPermissionDao = new AclGroupPermissionDao();
        aclGroupPermissionDao.addPermissions(aclGroup, changeRequest.getAdd());
        aclGroupPermissionDao.removePermissions(aclGroup, changeRequest.getRemove());
        AccessFilter.reload();
        cinnamonResponse.responseIsGenericOkay();
    }

    private void getUserPermissions(HttpServletRequest request, CinnamonResponse response) throws IOException {
        UserPermissionRequest permissionRequest = getMapper().readValue(request.getInputStream(), UserPermissionRequest.class).validateRequest()
                .orElseThrow(ErrorCode.INVALID_REQUEST.getException());

        long      userId   = permissionRequest.getUserId();
        long      aclId    = permissionRequest.getAclId();
        AclDao    aclDao   = new AclDao();
        List<Acl> userAcls = aclDao.getUserAcls(userId);
        if (userAcls == null) {
            ErrorResponseGenerator.generateErrorMessage(response, ErrorCode.PERMISSIONS_NOT_FOUND);
            return;
        }
        Optional<Acl>     optAcl  = userAcls.stream().filter(acl -> acl.getId().equals(aclId)).findFirst();
        PermissionWrapper wrapper = new PermissionWrapper();
        if (optAcl.isPresent()) {
            PermissionDao    dao             = new PermissionDao();
            List<Permission> userPermissions = dao.getUserPermissionForAcl(userId, aclId);
            wrapper.getPermissions().addAll(userPermissions);
        }
        response.setWrapper(wrapper);
    }

    @Override
    public ObjectMapper getMapper() {
        return xmlMapper;
    }
}
