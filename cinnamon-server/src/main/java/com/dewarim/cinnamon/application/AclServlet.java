package com.dewarim.cinnamon.application;

import com.dewarim.cinnamon.dao.AclDao;
import com.dewarim.cinnamon.dao.UserAccountDao;
import com.dewarim.cinnamon.model.Acl;
import com.dewarim.cinnamon.model.request.CreateAclRequest;
import com.dewarim.cinnamon.model.response.AclWrapper;
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
        switch (pathInfo) {
            case "/getAcls":
                listAcls(response);
                break;
            case  "/createAcl":createAcl(request,response); 
            default:
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }

    }

    private void listAcls(HttpServletResponse response) throws IOException {
        AclDao aclDao = new AclDao();
        List<Acl> acls = aclDao.list();
        AclWrapper aclWrapper = new AclWrapper();
        aclWrapper.setAcls(acls);
        response.setContentType(CONTENT_TYPE_XML);
        response.setStatus(HttpServletResponse.SC_OK);
        xmlMapper.writeValue(response.getWriter(), aclWrapper);
    }

    private void createAcl(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if(!UserAccountDao.currentUserIsSuperuser()){
            ErrorResponseGenerator.generateErrorMessage(response,HttpServletResponse.SC_FORBIDDEN,
                    ErrorCode.REQUIRES_SUPERUSER_STATUS,"");
            return;
        }

        CreateAclRequest aclRequest = xmlMapper.readValue(request.getInputStream(), CreateAclRequest.class);
        String name = aclRequest.getName();
        if(name == null || name.trim().isEmpty()){
           ErrorResponseGenerator.generateErrorMessage(response,HttpServletResponse.SC_BAD_REQUEST,
                   ErrorCode.NAME_PARAM_IS_INVALID,""); 
           return;
        }
        Acl acl = new Acl();
        acl.setName(name);
        AclDao aclDao = new AclDao();
        Acl savedAcl = aclDao.save(acl);
        AclWrapper aclWrapper = new AclWrapper();
        aclWrapper.setAcls(Collections.singletonList(savedAcl));
        response.setContentType(CONTENT_TYPE_XML);
        response.setStatus(HttpServletResponse.SC_OK);
        xmlMapper.writeValue(response.getWriter(), aclWrapper);        
    }

}
