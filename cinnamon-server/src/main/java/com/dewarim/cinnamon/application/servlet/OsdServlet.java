package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.DefaultPermission;
import com.dewarim.cinnamon.application.ErrorCode;
import com.dewarim.cinnamon.application.ErrorResponseGenerator;
import com.dewarim.cinnamon.dao.LinkDao;
import com.dewarim.cinnamon.model.links.Link;
import com.dewarim.cinnamon.model.request.IdListRequest;
import com.dewarim.cinnamon.model.request.OsdByFolderRequest;
import com.dewarim.cinnamon.model.request.SetSummaryRequest;
import com.dewarim.cinnamon.model.response.GenericResponse;
import com.dewarim.cinnamon.model.response.SummaryWrapper;
import com.dewarim.cinnamon.security.authorization.AuthorizationService;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.dao.OsdDao;
import com.dewarim.cinnamon.model.ObjectSystemData;
import com.dewarim.cinnamon.model.UserAccount;
import com.dewarim.cinnamon.model.request.OsdRequest;
import com.dewarim.cinnamon.model.response.OsdWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.http.HttpStatus;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static com.dewarim.cinnamon.Constants.CONTENT_TYPE_XML;

@WebServlet(name = "Osd", urlPatterns = "/")
public class OsdServlet extends HttpServlet {

    private ObjectMapper         xmlMapper            = new XmlMapper();
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
            case "/setSummary":
                setSummary(request, response);
                break;
            case "/getSummaries":
                getSummaries(request, response);
                break;
            default:
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }

    }

    private void setSummary(HttpServletRequest request, HttpServletResponse response) throws IOException {
        SetSummaryRequest          summaryRequest = xmlMapper.readValue(request.getInputStream(), SetSummaryRequest.class);
        OsdDao                     osdDao         = new OsdDao();
        UserAccount                user           = ThreadLocalSqlSession.getCurrentUser();
        Optional<ObjectSystemData> osdOpt         = osdDao.getObjectById(summaryRequest.getId());
        if (osdOpt.isPresent()) {
            ObjectSystemData osd = osdOpt.get();
            if (authorizationService.userHasPermission(osd.getAclId(), DefaultPermission.WRITE_OBJECT_SYS_METADATA.getName(), user)
                || authorizationService.userHasOwnerPermission(osd.getAclId(), DefaultPermission.WRITE_OBJECT_SYS_METADATA.getName(), user)) {
                
                osd.setSummary(summaryRequest.getSummary());
                osdDao.updateOsd(osd);
                response.setContentType(CONTENT_TYPE_XML);
                response.setStatus(HttpServletResponse.SC_OK);
                xmlMapper.writeValue(response.getWriter(), new GenericResponse(true));
                return;
            }
            else {
                ErrorResponseGenerator.generateErrorMessage(response, HttpServletResponse.SC_FORBIDDEN, ErrorCode.NO_WRITE_SYS_METADATA_PERMISSION);
                return;
            }
        }
        ErrorResponseGenerator.generateErrorMessage(response, HttpServletResponse.SC_NOT_FOUND, ErrorCode.OBJECT_NOT_FOUND);
    }

    private void getSummaries(HttpServletRequest request, HttpServletResponse response) throws IOException {
        IdListRequest          idListRequest = xmlMapper.readValue(request.getInputStream(), IdListRequest.class);
        SummaryWrapper         wrapper       = new SummaryWrapper();
        OsdDao                 osdDao        = new OsdDao();
        UserAccount            user          = ThreadLocalSqlSession.getCurrentUser();
        List<ObjectSystemData> osds          = osdDao.getObjectsById(idListRequest.getIdList(), true);
        osds.forEach(osd -> {
            Long aclId = osd.getAclId();
            if (authorizationService.userHasPermission(aclId, DefaultPermission.READ_OBJECT_SYS_METADATA.getName(), user)
                || authorizationService.userHasOwnerPermission(aclId, DefaultPermission.READ_OBJECT_SYS_METADATA.getName(), user)
                    ) {
                wrapper.getSummaries().add(osd.getSummary());
            }
        });
        response.setContentType(CONTENT_TYPE_XML);
        response.setStatus(HttpServletResponse.SC_OK);
        xmlMapper.writeValue(response.getWriter(), wrapper);
    }

    private void getObjectsById(HttpServletRequest request, HttpServletResponse response) throws IOException {
        OsdRequest             osdRequest   = xmlMapper.readValue(request.getInputStream(), OsdRequest.class);
        OsdDao                 osdDao       = new OsdDao();
        UserAccount            user         = ThreadLocalSqlSession.getCurrentUser();
        List<ObjectSystemData> osds         = osdDao.getObjectsById(osdRequest.getIds(), osdRequest.isIncludeSummary());
        List<ObjectSystemData> filteredOsds = authorizationService.filterObjectsByBrowsePermission(osds, user);

        OsdWrapper wrapper = new OsdWrapper();
        wrapper.setOsds(filteredOsds);
        response.setContentType(CONTENT_TYPE_XML);
        response.setStatus(HttpServletResponse.SC_OK);
        xmlMapper.writeValue(response.getWriter(), wrapper);
    }

    private void getObjectsByFolderId(HttpServletRequest request, HttpServletResponse response) throws IOException {
        OsdByFolderRequest     osdRequest     = xmlMapper.readValue(request.getInputStream(), OsdByFolderRequest.class);
        Long                   folderId       = osdRequest.getFolderId();
        boolean                includeSummary = osdRequest.isIncludeSummary();
        OsdDao                 osdDao         = new OsdDao();
        UserAccount            user           = ThreadLocalSqlSession.getCurrentUser();
        List<ObjectSystemData> osds           = osdDao.getObjectsByFolderId(folderId, includeSummary);
        List<ObjectSystemData> filteredOsds   = authorizationService.filterObjectsByBrowsePermission(osds, user);

        LinkDao    linkDao       = new LinkDao();
        List<Link> links         = linkDao.getLinksByFolderId(folderId);
        List<Link> filteredLinks = authorizationService.filterLinksByBrowsePermission(links, user);

        OsdWrapper wrapper = new OsdWrapper();
        wrapper.setOsds(filteredOsds);
        wrapper.setLinks(filteredLinks);
        response.setContentType(CONTENT_TYPE_XML);
        response.setStatus(HttpServletResponse.SC_OK);
        xmlMapper.writeValue(response.getWriter(), wrapper);
    }

}
