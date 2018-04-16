package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.model.response.LinkWrapper;
import com.dewarim.cinnamon.security.authorization.AuthorizationService;
import com.dewarim.cinnamon.application.ErrorCode;
import com.dewarim.cinnamon.application.ErrorResponseGenerator;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.dao.FolderDao;
import com.dewarim.cinnamon.dao.LinkDao;
import com.dewarim.cinnamon.dao.OsdDao;
import com.dewarim.cinnamon.model.*;
import com.dewarim.cinnamon.model.request.LinkRequest;
import com.dewarim.cinnamon.model.response.LinkResponse;
import com.dewarim.cinnamon.security.authorization.BrowseAcls;
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
import java.util.Optional;

import static com.dewarim.cinnamon.Constants.CONTENT_TYPE_XML;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

@WebServlet(name = "Link", urlPatterns = "/")
public class LinkServlet extends HttpServlet {

    private ObjectMapper xmlMapper = new XmlMapper();
    private AuthorizationService authorizationService = new AuthorizationService();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            pathInfo = "";
        }
        switch (pathInfo) {
            case "/getLinkById":
                getLinkById(request, response);
                break;
            default:
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }

    }

    private void getLinkById(HttpServletRequest request, HttpServletResponse response) throws IOException {
        LinkRequest linkRequest = xmlMapper.readValue(request.getInputStream(), LinkRequest.class);
        if (!linkRequest.validated()) {
            ErrorResponseGenerator.generateErrorMessage(response, SC_BAD_REQUEST,
                    ErrorCode.ID_PARAM_IS_INVALID, "Id must be a positive integer value.");
            return;
        }
        LinkDao linkDao = new LinkDao();
        Optional<Link> linkOptional = linkDao.getLinkById(linkRequest.getId());
        if (!linkOptional.isPresent()) {
            ErrorResponseGenerator.generateErrorMessage(response, SC_NOT_FOUND, ErrorCode.OBJECT_NOT_FOUND, "");
            return;
        }

        boolean includeSummary = linkRequest.isIncludeSummary();
        Link link = linkOptional.get();
        
        // check the Link's ACL:
        UserAccount user = ThreadLocalSqlSession.getCurrentUser();
        List<Link> filteredLink = authorizationService.filterLinksByBrowsePermission(Collections.singletonList(link), user);
        if(filteredLink.isEmpty()){
            ErrorResponseGenerator.generateErrorMessage(response, SC_UNAUTHORIZED, ErrorCode.UNAUTHORIZED, "");
            return;
        }

        Optional<LinkResponse> linkResponse;
        switch (link.getType()) {
            case FOLDER:
                linkResponse = handleFolderLink(response, link, includeSummary);
                break;
            case OBJECT:
                linkResponse = handleOsdLink(response, link, includeSummary);
                break;
            default:
                throw new IllegalStateException("unknown link type");
        }

        if (linkResponse.isPresent()) {
            LinkWrapper wrapper = new LinkWrapper();
            wrapper.getLinks().add(linkResponse.get());
            response.setContentType(CONTENT_TYPE_XML);
            response.setStatus(HttpServletResponse.SC_OK);
            xmlMapper.writeValue(response.getWriter(), wrapper);
        }
    }

    private Optional<LinkResponse> handleFolderLink(HttpServletResponse response, Link link, boolean includeSummary) {
        BrowseAcls browseAcls = BrowseAcls.getInstance(ThreadLocalSqlSession.getCurrentUser());
        if (!browseAcls.hasFolderBrowsePermission(link.getAclId())) {
            ErrorResponseGenerator.generateErrorMessage(response, SC_UNAUTHORIZED, ErrorCode.UNAUTHORIZED, "");
            return Optional.empty();
        }
        FolderDao folderDao = new FolderDao();
        List<Folder> folders = folderDao.getFoldersById(Collections.singletonList(link.getFolderId()), includeSummary);
        // existence of folder should be guaranteed by foreign key constraing in DB.
        Folder folder = folders.get(0);
        if (browseAcls.hasFolderBrowsePermission(folder.getAclId())) {
            LinkResponse linkResponse = new LinkResponse();
            linkResponse.setLinkType(LinkType.FOLDER);
            linkResponse.setFolder(folder);
            return Optional.of(linkResponse);
        }
        ErrorResponseGenerator.generateErrorMessage(response, SC_UNAUTHORIZED, ErrorCode.UNAUTHORIZED, "");
        return Optional.empty();
    }

    private Optional<LinkResponse> handleOsdLink(HttpServletResponse response, Link link, boolean includeSummary) {
        BrowseAcls browseAcls = BrowseAcls.getInstance(ThreadLocalSqlSession.getCurrentUser());
        if (!browseAcls.hasUserBrowsePermission(link.getAclId())) {
            ErrorResponseGenerator.generateErrorMessage(response, SC_UNAUTHORIZED, ErrorCode.UNAUTHORIZED, "");
            return Optional.empty();
        }
        OsdDao osdDao = new OsdDao();
        List<ObjectSystemData> osds = osdDao.getObjectsById(Collections.singletonList(link.getObjectId()), includeSummary);
        ObjectSystemData osd = osds.get(0);
        if (browseAcls.hasUserBrowsePermission(osd.getAclId())) {
            LinkResponse linkResponse = new LinkResponse();
            linkResponse.setLinkType(LinkType.OBJECT);
            linkResponse.setOsd(osd);
            return Optional.of(linkResponse);

        }
        ErrorResponseGenerator.generateErrorMessage(response, SC_UNAUTHORIZED, ErrorCode.UNAUTHORIZED, "");
        return Optional.empty();
    }

}
