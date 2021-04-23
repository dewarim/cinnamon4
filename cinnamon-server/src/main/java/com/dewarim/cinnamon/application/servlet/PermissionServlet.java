package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.application.ErrorCode;
import com.dewarim.cinnamon.application.ErrorResponseGenerator;
import com.dewarim.cinnamon.dao.AclDao;
import com.dewarim.cinnamon.dao.PermissionDao;
import com.dewarim.cinnamon.model.Acl;
import com.dewarim.cinnamon.model.Permission;
import com.dewarim.cinnamon.model.request.user.UserPermissionRequest;
import com.dewarim.cinnamon.model.response.PermissionWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static com.dewarim.cinnamon.Constants.CONTENT_TYPE_XML;

/**
 *
 */
@WebServlet(name = "Permission", urlPatterns = "/")
public class PermissionServlet extends HttpServlet {

    private ObjectMapper xmlMapper = new XmlMapper();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            pathInfo = "";
        }
        switch (pathInfo) {
            case "/listPermissions":
                listPermissions(response);
                break;
            case "/getUserPermissions":
                getUserPermissions(xmlMapper.readValue(request.getReader(), UserPermissionRequest.class), response);
                break;
            default:
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }

    }

    private void listPermissions(HttpServletResponse response) throws IOException {
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
