package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.application.ErrorCode;
import com.dewarim.cinnamon.application.ErrorResponseGenerator;
import com.dewarim.cinnamon.dao.AclDao;
import com.dewarim.cinnamon.dao.UserAccountDao;
import com.dewarim.cinnamon.model.Acl;
import com.dewarim.cinnamon.model.request.*;
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
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;

@WebServlet(name = "Acl", urlPatterns = "/")
public class AclServlet extends HttpServlet {

    private ObjectMapper xmlMapper = new XmlMapper();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            pathInfo = "";
        }
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
                updateAcl(request,response);
                break;
            default:
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }

    }

    private void updateAcl(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!UserAccountDao.currentUserIsSuperuser()) {
            ErrorResponseGenerator.superuserRequired(response);
            return;
        }
        AclUpdateRequest updateRequest = xmlMapper.readValue(request.getInputStream(),AclUpdateRequest.class);
        String name = updateRequest.getName();
        if (name == null || name.trim().isEmpty()) {
            ErrorResponseGenerator.generateErrorMessage(response, SC_BAD_REQUEST,
                    ErrorCode.NAME_PARAM_IS_INVALID, "");
            return;
        }
        AclDao aclDao = new AclDao();
        Acl aclToBeRenamed = aclDao.getAclById(updateRequest.getId());
        aclToBeRenamed.setName(name);
        try {
            aclDao.changeAclName(aclToBeRenamed);
        }
        catch (Exception e){
            ErrorResponseGenerator.generateErrorMessage(response,SC_BAD_REQUEST,ErrorCode.DB_UPDATE_FAILED,e.getMessage());
            return;
        }
        sendWrappedAcls(response, Collections.singletonList(aclToBeRenamed));
    }

    private void listAcls(HttpServletResponse response) throws IOException {
        AclDao aclDao = new AclDao();
        List<Acl> acls = aclDao.list();
        sendWrappedAcls(response,acls);
    }

    private void createAcl(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!UserAccountDao.currentUserIsSuperuser()) {
            ErrorResponseGenerator.superuserRequired(response);
            return;
        }

        CreateAclRequest aclRequest = xmlMapper.readValue(request.getInputStream(), CreateAclRequest.class);
        String name = aclRequest.getName();
        if (name == null || name.trim().isEmpty()) {
            ErrorResponseGenerator.generateErrorMessage(response, SC_BAD_REQUEST,
                    ErrorCode.NAME_PARAM_IS_INVALID, "");
            return;
        }
        Acl acl = new Acl();
        acl.setName(name);
        AclDao aclDao = new AclDao();
        Acl savedAcl = aclDao.save(acl);
        sendWrappedAcls(response,Collections.singletonList(savedAcl));
    }

    private void getAclByNameOrId(HttpServletRequest request, HttpServletResponse response) throws IOException {
        AclInfoRequest aclInfoRequest = xmlMapper.readValue(request.getInputStream(), AclInfoRequest.class);
        AclDao aclDao = new AclDao();
        Acl acl;
        if (aclInfoRequest.byId()) {
            acl = aclDao.getAclById(aclInfoRequest.getAclId());
        }
        else if (aclInfoRequest.byName()) {
            acl = aclDao.getAclByName(aclInfoRequest.getName());
        }
        else {
            ErrorResponseGenerator.generateErrorMessage(response, SC_BAD_REQUEST,
                    ErrorCode.INFO_REQUEST_WITHOUT_NAME_OR_ID, "Request needs id or name to be set.");
            return;
        }
        sendWrappedAcls(response, Collections.singletonList(acl));
    }

    private void deleteById(HttpServletRequest request, HttpServletResponse response) throws IOException {
        DeleteByIdRequest deletionRequest = xmlMapper.readValue(request.getInputStream(), DeleteByIdRequest.class);
        if (!UserAccountDao.currentUserIsSuperuser()) {
            ErrorResponseGenerator.superuserRequired(response);
            return;
        }

        Long id = deletionRequest.getId();
        if (id == null) {
            ErrorResponseGenerator.generateErrorMessage(response, SC_BAD_REQUEST,
                    ErrorCode.DELETE_REQUEST_WITHOUT_ID, "Request needs id parameter.");
            return;
        }

        DeletionResponse deletionResponse = new DeletionResponse();
        AclDao aclDao = new AclDao();
        Acl acl = aclDao.getAclById(id);
        if (acl == null) {
            deletionResponse.setNotFound(true);
        }
        else {
            int deletedRows = aclDao.deleteAcl(deletionRequest.getId());
            deletionResponse.setSuccess(deletedRows > 0);
        }
        
        response.setContentType(CONTENT_TYPE_XML);
        response.setStatus(HttpServletResponse.SC_OK);
        xmlMapper.writeValue(response.getWriter(), deletionResponse);
    }

    private void getUserAcls(HttpServletRequest request, HttpServletResponse response) throws IOException{
        IdRequest idRequest = xmlMapper.readValue(request.getInputStream(), IdRequest.class);
        Long userId = idRequest.getId(); 
        if(userId == null || userId < 1 ){
            ErrorResponseGenerator.generateErrorMessage(response,SC_BAD_REQUEST,ErrorCode.ID_PARAM_IS_INVALID,"");
            return;
        }
        AclDao aclDao = new AclDao();
        List<Acl> userAcls = aclDao.getUserAcls(userId);
        sendWrappedAcls(response,userAcls);
    }
    
    private void sendWrappedAcls(HttpServletResponse response, List<Acl> acls) throws IOException{
        AclWrapper aclWrapper = new AclWrapper();
        aclWrapper.getAcls().addAll(acls);
        aclWrapper.getAcls().removeAll(Collections.singleton(null));
        response.setContentType(CONTENT_TYPE_XML);
        response.setStatus(HttpServletResponse.SC_OK);
        xmlMapper.writeValue(response.getWriter(), aclWrapper);
    }
    
}
