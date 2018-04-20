package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.dao.LinkDao;
import com.dewarim.cinnamon.model.Link;
import com.dewarim.cinnamon.model.request.OsdByFolderRequest;
import com.dewarim.cinnamon.security.authorization.AuthorizationService;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.dao.OsdDao;
import com.dewarim.cinnamon.model.ObjectSystemData;
import com.dewarim.cinnamon.model.UserAccount;
import com.dewarim.cinnamon.model.request.OsdRequest;
import com.dewarim.cinnamon.model.response.OsdWrapper;
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

@WebServlet(name = "Osd", urlPatterns = "/")
public class OsdServlet extends HttpServlet {

    private ObjectMapper xmlMapper = new XmlMapper();
    private AuthorizationService authorizationService = new AuthorizationService();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            pathInfo = "";
        }
        switch (pathInfo) {
            case "/getObjectsByFolderId":
                getObjectsByFolderId(request, response);
                break;
            case "/getObjectsById":
                getObjectsById(request, response);
                break;
            default:
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }

    }

    private void getObjectsById(HttpServletRequest request, HttpServletResponse response) throws IOException {
        OsdRequest osdRequest = xmlMapper.readValue(request.getInputStream(), OsdRequest.class);
        OsdDao osdDao = new OsdDao();
        UserAccount user = ThreadLocalSqlSession.getCurrentUser();
        List<ObjectSystemData> osds = osdDao.getObjectsById(osdRequest.getIds(), osdRequest.isIncludeSummary());
        List<ObjectSystemData> filteredOsds = authorizationService.filterObjectsByBrowsePermission(osds, user);

        OsdWrapper wrapper = new OsdWrapper();
        wrapper.setOsds(filteredOsds);
        response.setContentType(CONTENT_TYPE_XML);
        response.setStatus(HttpServletResponse.SC_OK);
        xmlMapper.writeValue(response.getWriter(), wrapper);
    }
    
    private void getObjectsByFolderId(HttpServletRequest request, HttpServletResponse response) throws IOException {
        OsdByFolderRequest osdRequest = xmlMapper.readValue(request.getInputStream(), OsdByFolderRequest.class);
        Long folderId = osdRequest.getFolderId();
        boolean includeSummary = osdRequest.isIncludeSummary();
        OsdDao osdDao = new OsdDao();
        UserAccount user = ThreadLocalSqlSession.getCurrentUser();
        List<ObjectSystemData> osds = osdDao.getObjectsByFolderId(folderId, includeSummary);
        List<ObjectSystemData> filteredOsds = authorizationService.filterObjectsByBrowsePermission(osds, user);
        
        LinkDao linkDao = new LinkDao();
        List<Link> links = linkDao.getLinksByFolderId(folderId);
        List<Link> filteredLinks = authorizationService.filterLinksByBrowsePermission(links, user);
        
        OsdWrapper wrapper = new OsdWrapper();
        wrapper.setOsds(filteredOsds);
        wrapper.setLinks(filteredLinks);
        response.setContentType(CONTENT_TYPE_XML);
        response.setStatus(HttpServletResponse.SC_OK);
        xmlMapper.writeValue(response.getWriter(), wrapper);
    }

}
