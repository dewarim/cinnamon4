package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.application.ErrorResponseGenerator;
import com.dewarim.cinnamon.dao.AclDao;
import com.dewarim.cinnamon.dao.PermissionDao;
import com.dewarim.cinnamon.model.Acl;
import com.dewarim.cinnamon.model.Permission;
import com.dewarim.cinnamon.model.request.ListPermissionRequest;
import com.dewarim.cinnamon.model.request.ListRequest;
import com.dewarim.cinnamon.model.request.user.UserPermissionRequest;
import com.dewarim.cinnamon.model.response.PermissionWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.deser.FromXmlParser;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static com.dewarim.cinnamon.api.Constants.CONTENT_TYPE_XML;

/**
 *
 */
@WebServlet(name = "Permission", urlPatterns = "/")
public class PermissionServlet extends HttpServlet {

    private final ObjectMapper xmlMapper = new XmlMapper().configure(FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL, true);

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            pathInfo = "";
        }
        switch (pathInfo) {
            case "/listPermissions":
                listPermissions(request, response);
                break;
            case "/getUserPermissions":
                getUserPermissions(xmlMapper.readValue(request.getReader(), UserPermissionRequest.class), response);
                break;
            default:
                ErrorCode.RESOURCE_NOT_FOUND.throwUp();
        }

    }

    private void listPermissions(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ListRequest       listRequest = xmlMapper.readValue(request.getInputStream(), ListPermissionRequest.class);
        List<Permission>  permissions = new PermissionDao().listPermissions();
        PermissionWrapper wrapper     = new PermissionWrapper();
        wrapper.setPermissions(permissions);
        response.setContentType(CONTENT_TYPE_XML);
        response.setStatus(HttpServletResponse.SC_OK);
        xmlMapper.writeValue(response.getWriter(), wrapper);
    }

    private void getUserPermissions(UserPermissionRequest permissionRequest, HttpServletResponse response) throws IOException {
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
        response.setContentType(CONTENT_TYPE_XML);
        response.setStatus(HttpServletResponse.SC_OK);
        xmlMapper.writeValue(response.getWriter(), wrapper);
    }
}
