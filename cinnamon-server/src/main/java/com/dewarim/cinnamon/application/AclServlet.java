package com.dewarim.cinnamon.application;

import com.dewarim.cinnamon.dao.AclDao;
import com.dewarim.cinnamon.model.Acl;
import com.dewarim.cinnamon.model.response.AclWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import static com.dewarim.cinnamon.Constants.CONTENT_TYPE_XML;

@WebServlet(name = "Acl", urlPatterns = "/")
public class AclServlet extends HttpServlet{

    private ObjectMapper xmlMapper = new XmlMapper();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String pathInfo = request.getPathInfo();
        if(pathInfo == null){
            pathInfo = "";
        }
        switch (pathInfo) {
            case "/getAcls":
                listAcls(response);
                break;
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
    
}
