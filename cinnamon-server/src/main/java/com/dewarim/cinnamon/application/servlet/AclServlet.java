package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.application.ErrorCode;
import com.dewarim.cinnamon.application.ErrorResponseGenerator;
import com.dewarim.cinnamon.application.exception.FailedRequestException;
import com.dewarim.cinnamon.dao.AclDao;
import com.dewarim.cinnamon.dao.UserAccountDao;
import com.dewarim.cinnamon.model.Acl;
import com.dewarim.cinnamon.model.request.DeleteByIdRequest;
import com.dewarim.cinnamon.model.request.IdRequest;
import com.dewarim.cinnamon.model.request.acl.AclInfoRequest;
import com.dewarim.cinnamon.model.request.acl.AclUpdateRequest;
import com.dewarim.cinnamon.model.request.acl.CreateAclRequest;
import com.dewarim.cinnamon.model.response.AclWrapper;
import com.dewarim.cinnamon.model.response.DeletionResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static com.dewarim.cinnamon.Constants.CONTENT_TYPE_XML;

@WebServlet(name = "Acl", urlPatterns = "/")
public class AclServlet extends HttpServlet {

    private ObjectMapper xmlMapper = new XmlMapper();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            pathInfo = "";
        }
        try {
            switch (pathInfo) {
                case "/createAcl":
                    createAcl(request, response);
                    break;
                case "/aclInfo":
                    getAclByNameOrId(request, response);
                    break;
                case "/deleteAcl":
                    deleteById(request, response);
                    break;
                case "/getAcls":
                    listAcls(response);
                    break;
                case "/getUserAcls":
                    getUserAcls(request, response);
                    break;
                case "/updateAcl":
                    updateAcl(request, response);
                    break;
                default:
                    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            }
        } catch (FailedRequestException e) {
            ErrorCode errorCode = e.getErrorCode();
            ErrorResponseGenerator.generateErrorMessage(response, errorCode.getHttpResponseCode(), errorCode, e.getMessage());
        }

    }

    private void updateAcl(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!UserAccountDao.currentUserIsSuperuser()) {
            ErrorCode.REQUIRES_SUPERUSER_STATUS.throwUp();
        }
        AclUpdateRequest updateRequest = xmlMapper.readValue(request.getInputStream(), AclUpdateRequest.class);
        String           name          = updateRequest.getName();
        if (name == null || name.trim().isEmpty()) {
            ErrorCode.NAME_PARAM_IS_INVALID.throwUp();
        }
        AclDao aclDao         = new AclDao();
        Acl    aclToBeRenamed = aclDao.getAclByIdOpt(updateRequest.getId()).orElseThrow(ErrorCode.ACL_NOT_FOUND.getException());
        aclToBeRenamed.setName(name);
        try {
            aclDao.changeAclName(aclToBeRenamed);
        } catch (Exception e) {
            throw new FailedRequestException(ErrorCode.DB_UPDATE_FAILED, e.getMessage());
        }
        sendWrappedAcls(response, Collections.singletonList(aclToBeRenamed));
    }

    private void listAcls(HttpServletResponse response) throws IOException {
        AclDao    aclDao = new AclDao();
        List<Acl> acls   = aclDao.list();
        sendWrappedAcls(response, acls);
    }

    private void createAcl(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!UserAccountDao.currentUserIsSuperuser()) {
            ErrorCode.REQUIRES_SUPERUSER_STATUS.throwUp();
        }

        CreateAclRequest aclRequest = xmlMapper.readValue(request.getInputStream(), CreateAclRequest.class);
        String           name       = aclRequest.getName();
        if (name == null || name.trim().isEmpty()) {
            ErrorCode.NAME_PARAM_IS_INVALID.throwUp();
        }
        Acl acl = new Acl();
        acl.setName(name);
        AclDao aclDao   = new AclDao();
        Acl    savedAcl = aclDao.save(acl);
        sendWrappedAcls(response, Collections.singletonList(savedAcl));
    }

    private void getAclByNameOrId(HttpServletRequest request, HttpServletResponse response) throws IOException {
        AclInfoRequest aclInfoRequest = xmlMapper.readValue(request.getInputStream(), AclInfoRequest.class);
        AclDao         aclDao         = new AclDao();
        Acl            acl;
        if (aclInfoRequest.byId()) {
            acl = aclDao.getAclByIdOpt(aclInfoRequest.getAclId()).orElseThrow(ErrorCode.ACL_NOT_FOUND.getException());
        } else if (aclInfoRequest.byName()) {
            acl = aclDao.getAclByName(aclInfoRequest.getName());
        } else {
            ErrorCode.INFO_REQUEST_WITHOUT_NAME_OR_ID.throwUp();
            return;
        }
        sendWrappedAcls(response, Collections.singletonList(acl));
    }

    private void deleteById(HttpServletRequest request, HttpServletResponse response) throws IOException {
        DeleteByIdRequest deletionRequest = xmlMapper.readValue(request.getInputStream(), DeleteByIdRequest.class);
        if (!UserAccountDao.currentUserIsSuperuser()) {
            ErrorCode.REQUIRES_SUPERUSER_STATUS.throwUp();
        }

        Long id = deletionRequest.getId();
        if (id == null) {
            ErrorCode.DELETE_REQUEST_WITHOUT_ID.throwUp();
        }

        DeletionResponse deletionResponse = new DeletionResponse();
        AclDao           aclDao           = new AclDao();
        Acl              acl              = aclDao.getAclByIdOpt(id).orElseThrow(ErrorCode.ACL_NOT_FOUND.getException());
        int              deletedRows      = aclDao.deleteAcl(acl.getId());
        deletionResponse.setSuccess(deletedRows == 1);

        response.setContentType(CONTENT_TYPE_XML);
        response.setStatus(HttpServletResponse.SC_OK);
        xmlMapper.writeValue(response.getWriter(), deletionResponse);
    }

    private void getUserAcls(HttpServletRequest request, HttpServletResponse response) throws IOException {
        IdRequest idRequest = xmlMapper.readValue(request.getInputStream(), IdRequest.class);
        Long      userId    = idRequest.getId();
        if (userId == null || userId < 1) {
            ErrorCode.ID_PARAM_IS_INVALID.throwUp();
        }
        AclDao    aclDao   = new AclDao();
        List<Acl> userAcls = aclDao.getUserAcls(userId);
        sendWrappedAcls(response, userAcls);
    }

    private void sendWrappedAcls(HttpServletResponse response, List<Acl> acls) throws IOException {
        AclWrapper aclWrapper = new AclWrapper();
        aclWrapper.getAcls().addAll(acls);
        aclWrapper.getAcls().removeAll(Collections.singleton(null));
        response.setContentType(CONTENT_TYPE_XML);
        response.setStatus(HttpServletResponse.SC_OK);
        xmlMapper.writeValue(response.getWriter(), aclWrapper);
    }

}
