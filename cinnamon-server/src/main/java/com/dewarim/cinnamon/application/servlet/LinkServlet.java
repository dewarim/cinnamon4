package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.DefaultPermissions;
import com.dewarim.cinnamon.dao.AclDao;
import com.dewarim.cinnamon.model.request.CreateLinkRequest;
import com.dewarim.cinnamon.model.request.DeleteByIdRequest;
import com.dewarim.cinnamon.model.response.DeletionResponse;
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
            case "/createLink":
                createLink(request, response);
                break;
            case "/deleteLink":
                deleteLink(request, response);
                break;
            case "/getLinkById":
                getLinkById(request, response);
                break;
            default:
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }

    }

    private void createLink(HttpServletRequest request, HttpServletResponse response) throws IOException {
        CreateLinkRequest linkRequest = xmlMapper.readValue(request.getInputStream(), CreateLinkRequest.class);
        if (!linkRequest.validated()) {
            ErrorResponseGenerator.generateErrorMessage(response, SC_BAD_REQUEST, ErrorCode.INVALID_REQUEST);
            return;
        }
        UserAccount user = ThreadLocalSqlSession.getCurrentUser();
        FolderDao folderDao = new FolderDao();
        List<Folder> parentFolders = folderDao.getFoldersById(Collections.singletonList(linkRequest.getParentId()), false);
        if (parentFolders.isEmpty()) {
            ErrorResponseGenerator.generateErrorMessage(response, SC_BAD_REQUEST, ErrorCode.PARENT_FOLDER_NOT_FOUND);
            return;
        }
        Folder parentFolder = parentFolders.get(0);
        BrowseAcls browseAcls = BrowseAcls.getInstance(user);
        boolean browsePermission = browseAcls.hasFolderBrowsePermission(parentFolder.getAclId());
        boolean writePermission = browseAcls.hasPermission(parentFolder.getAclId(), DefaultPermissions.CREATE_OBJECT.getName());
        if (!(browsePermission && writePermission)) {
            ErrorResponseGenerator.generateErrorMessage(response, SC_UNAUTHORIZED, ErrorCode.UNAUTHORIZED);
            return;
        }
        AclDao aclDao = new AclDao();
        Acl acl = aclDao.getAclById(linkRequest.getAclId());
        if (acl == null) {
            ErrorResponseGenerator.generateErrorMessage(response, SC_BAD_REQUEST, ErrorCode.ACL_NOT_FOUND);
            return;
        }

        Folder folder = null;
        ObjectSystemData osd = null;
        OsdDao osdDao = new OsdDao();
        boolean hasBrowsePermission;
        switch (linkRequest.getLinkType()) {
            case FOLDER:
                folder = folderDao.getFolderById(linkRequest.getId());
                hasBrowsePermission = browseAcls.hasFolderBrowsePermission(folder.getAclId());
                break;
            case OBJECT:
                osd = osdDao.getObjectById(linkRequest.getId());
                hasBrowsePermission = browseAcls.hasBrowsePermissionForOsd(osd);
                break;
            default:
                throw new IllegalStateException("invalid link type: " + linkRequest.getLinkType());
        }

        if (!hasBrowsePermission) {
            ErrorResponseGenerator.generateErrorMessage(response, SC_UNAUTHORIZED, ErrorCode.UNAUTHORIZED);
            return;
        }

        LinkDao linkDao = new LinkDao();
        Link link = linkDao.createLink(linkRequest);
        LinkResponse linkResponse = new LinkResponse();
        linkResponse.setLinkType(link.getType());
        linkResponse.setOsd(osd);
        linkResponse.setFolder(folder);
        LinkWrapper linkWrapper = new LinkWrapper();
        linkWrapper.getLinks().add(linkResponse);
        response.setContentType(CONTENT_TYPE_XML);
        response.setStatus(HttpServletResponse.SC_OK);
        xmlMapper.writeValue(response.getWriter(), linkWrapper);
    }


    private void deleteLink(HttpServletRequest request, HttpServletResponse response) throws IOException {
        DeleteByIdRequest deleteRequest = xmlMapper.readValue(request.getInputStream(), DeleteByIdRequest.class);
        if (!deleteRequest.validated()) {
            ErrorResponseGenerator.generateErrorMessage(response, SC_BAD_REQUEST,
                    ErrorCode.ID_PARAM_IS_INVALID, "Id must be a positive integer value.");
            return;
        }
        LinkDao linkDao = new LinkDao();
        Optional<Link> linkOptional = linkDao.getLinkById(deleteRequest.getId());
        DeletionResponse deletionResponse = new DeletionResponse();
        if (linkOptional.isPresent()) {
            Link link = linkOptional.get();
            UserAccount user = ThreadLocalSqlSession.getCurrentUser();
            List<Link> filteredLink = authorizationService.filterLinksByBrowsePermission(Collections.singletonList(link), user);
            if (filteredLink.isEmpty()) {
                ErrorResponseGenerator.generateErrorMessage(response, SC_UNAUTHORIZED, ErrorCode.UNAUTHORIZED, "");
                return;
            }

            boolean deleteOkay;
            switch (link.getType()) {
                case FOLDER:
                    deleteOkay = authorizationService.userHasPermission(link.getAclId(), DefaultPermissions.DELETE_FOLDER.getName(), user);
                    break;
                case OBJECT:
                    deleteOkay = authorizationService.userHasPermission(link.getAclId(), DefaultPermissions.DELETE_OBJECT.getName(), user);
                    break;
                default:
                    throw new IllegalStateException("unknown link type");
            }
            if (!deleteOkay) {
                ErrorResponseGenerator.generateErrorMessage(response, SC_UNAUTHORIZED, ErrorCode.UNAUTHORIZED, "");
                return;
            }
            int deletedRows = linkDao.deleteLink(link.getId());
            deletionResponse.setSuccess(deletedRows == 1);
        }
        else {
            deletionResponse.setNotFound(true);
        }
        response.setContentType(CONTENT_TYPE_XML);
        response.setStatus(HttpServletResponse.SC_OK);
        xmlMapper.writeValue(response.getWriter(), deletionResponse);
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
        if (filteredLink.isEmpty()) {
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
        ObjectSystemData osd;
        switch (link.getResolver()) {
            case LATEST_HEAD:
                osd = osdDao.getLatestHead(link.getObjectId());
                break;
            case FIXED: // fall through to default - fixed is standard case.
            default:
                List<ObjectSystemData> osds = osdDao.getObjectsById(Collections.singletonList(link.getObjectId()), includeSummary);
                osd = osds.get(0);
                break;
        }

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
