package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.DefaultPermission;
import com.dewarim.cinnamon.application.ErrorCode;
import com.dewarim.cinnamon.application.ErrorResponseGenerator;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.application.exception.FailedRequestException;
import com.dewarim.cinnamon.application.exception.UpdateException;
import com.dewarim.cinnamon.dao.*;
import com.dewarim.cinnamon.model.Acl;
import com.dewarim.cinnamon.model.Folder;
import com.dewarim.cinnamon.model.ObjectSystemData;
import com.dewarim.cinnamon.model.UserAccount;
import com.dewarim.cinnamon.model.links.Link;
import com.dewarim.cinnamon.model.links.LinkType;
import com.dewarim.cinnamon.model.request.CreateLinkRequest;
import com.dewarim.cinnamon.model.request.DeleteByIdRequest;
import com.dewarim.cinnamon.model.request.LinkRequest;
import com.dewarim.cinnamon.model.request.LinkUpdateRequest;
import com.dewarim.cinnamon.model.response.DeletionResponse;
import com.dewarim.cinnamon.model.response.GenericResponse;
import com.dewarim.cinnamon.model.response.LinkResponse;
import com.dewarim.cinnamon.model.response.LinkWrapper;
import com.dewarim.cinnamon.security.authorization.AccessFilter;
import com.dewarim.cinnamon.security.authorization.AuthorizationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.dewarim.cinnamon.Constants.CONTENT_TYPE_XML;
import static com.dewarim.cinnamon.application.exception.UpdateException.*;
import static javax.servlet.http.HttpServletResponse.*;

@WebServlet(name = "Link", urlPatterns = "/")
public class LinkServlet extends HttpServlet {

    private static final Logger log             = LogManager.getLogger(LinkServlet.class);
    private static final int    UPDATED_ONE_ROW = 1;

    private ObjectMapper         xmlMapper            = new XmlMapper();
    private AuthorizationService authorizationService = new AuthorizationService();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            pathInfo = "";
        }
        try {
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
                case "/updateLink":
                    updateLink(request, response);
                    break;
                default:
                    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            }
        } catch (FailedRequestException e) {
            ErrorCode errorCode = e.getErrorCode();
            ErrorResponseGenerator.generateErrorMessage(response, errorCode.getHttpResponseCode(), errorCode);
        }

    }

    private void createLink(HttpServletRequest request, HttpServletResponse response) throws IOException {
        CreateLinkRequest linkRequest = xmlMapper.readValue(request.getInputStream(), CreateLinkRequest.class);
        if (!linkRequest.validated()) {
            ErrorResponseGenerator.generateErrorMessage(response, ErrorCode.INVALID_REQUEST);
            return;
        }

        UserAccount  user          = ThreadLocalSqlSession.getCurrentUser();
        FolderDao    folderDao     = new FolderDao();
        List<Folder> parentFolders = folderDao.getFoldersById(Collections.singletonList(linkRequest.getParentId()), false);
        if (parentFolders.isEmpty()) {
            ErrorResponseGenerator.generateErrorMessage(response, ErrorCode.PARENT_FOLDER_NOT_FOUND);
            return;
        }

        Folder       parentFolder     = parentFolders.get(0);
        AccessFilter accessFilter     = AccessFilter.getInstance(user);
        boolean      browsePermission = accessFilter.hasPermissionOnOwnable(parentFolder, DefaultPermission.BROWSE_FOLDER, parentFolder);
        boolean      writePermission  = accessFilter.hasPermissionOnOwnable(parentFolder, DefaultPermission.CREATE_OBJECT, parentFolder);
        if (!(browsePermission && writePermission)) {
            ErrorResponseGenerator.generateErrorMessage(response, ErrorCode.UNAUTHORIZED);
            return;
        }

        AclDao aclDao = new AclDao();
        Acl    acl    = aclDao.getAclById(linkRequest.getAclId());
        if (acl == null) {
            ErrorResponseGenerator.generateErrorMessage(response, ErrorCode.ACL_NOT_FOUND);
            return;
        }

        // check if owner of new link exists:
        UserAccountDao        userDao  = new UserAccountDao();
        Optional<UserAccount> ownerOpt = userDao.getUserAccountById(linkRequest.getOwnerId());
        if (!ownerOpt.isPresent()) {
            ErrorResponseGenerator.generateErrorMessage(response, ErrorCode.OWNER_NOT_FOUND);
            return;
        }

        Folder           folder = null;
        ObjectSystemData osd    = null;
        OsdDao           osdDao = new OsdDao();
        boolean          hasBrowsePermission;
        switch (linkRequest.getLinkType()) {
            case FOLDER:
                Optional<Folder> folderOpt = folderDao.getFolderById(linkRequest.getId());
                if (folderOpt.isPresent()) {
                    folder = folderOpt.get();
                    hasBrowsePermission = accessFilter.hasPermissionOnOwnable(folder, DefaultPermission.BROWSE_FOLDER, folder);
                } else {
                    ErrorResponseGenerator.generateErrorMessage(response, SC_NOT_FOUND, ErrorCode.FOLDER_NOT_FOUND);
                    return;
                }
                break;
            case OBJECT:
                Optional<ObjectSystemData> osdOpt = osdDao.getObjectById(linkRequest.getId());
                // TODO: check if this isPresent can be replaced with orThrow and response generation upstream.
                if (osdOpt.isPresent()) {
                    osd = osdOpt.get();
                    hasBrowsePermission = accessFilter.hasPermissionOnOwnable(osd, DefaultPermission.BROWSE_OBJECT, osd);
                } else {
                    ErrorResponseGenerator.generateErrorMessage(response, SC_NOT_FOUND, ErrorCode.OBJECT_NOT_FOUND);
                    return;
                }
                break;
            default:
                throw new IllegalStateException("invalid link type: " + linkRequest.getLinkType());
        }

        if (!hasBrowsePermission) {
            ErrorResponseGenerator.generateErrorMessage(response, SC_UNAUTHORIZED, ErrorCode.UNAUTHORIZED);
            return;
        }

        LinkDao      linkDao      = new LinkDao();
        Link         link         = linkDao.createLink(linkRequest);
        LinkResponse linkResponse = new LinkResponse();
        linkResponse.setLinkType(link.getType());
        linkResponse.setOsd(osd);
        linkResponse.setFolder(folder);
        linkResponse.setOwnerId(link.getOwnerId());
        linkResponse.setParentId(link.getParentId());
        linkResponse.setAclId(link.getAclId());
        LinkWrapper linkWrapper = new LinkWrapper();
        linkWrapper.getLinks().add(linkResponse);
        response.setContentType(CONTENT_TYPE_XML);
        response.setStatus(HttpServletResponse.SC_OK);
        xmlMapper.writeValue(response.getWriter(), linkWrapper);
    }


    private void updateLink(HttpServletRequest request, HttpServletResponse response) throws IOException {
        LinkUpdateRequest updateRequest = xmlMapper.readValue(request.getInputStream(), LinkUpdateRequest.class);
        if (!updateRequest.validated()) {
            ErrorResponseGenerator.generateErrorMessage(response, SC_BAD_REQUEST,
                    ErrorCode.INVALID_REQUEST, "Id must be a positive integer value and one or more fields must be set.");
            return;
        }
        LinkDao        linkDao      = new LinkDao();
        Optional<Link> linkOptional = linkDao.getLinkById(updateRequest.getId());
        if (linkOptional.isPresent()) {
            Link         link         = linkOptional.get();
            UserAccount  user         = ThreadLocalSqlSession.getCurrentUser();
            AccessFilter accessFilter = AccessFilter.getInstance(user);

            try {
                // no update allowed for links that the user cannot even see:
                if (!accessFilter.hasPermissionOnOwnable(link, DefaultPermission.BROWSE_OBJECT, link)) {
                    throw NO_BROWSE_PERMISSION;
                }

                if (!accessFilter.hasPermissionOnOwnable(link, DefaultPermission.WRITE_OBJECT_SYS_METADATA, link)) {
                    throw MISSING_SET_SYSMETA_PERMISSION;
                }

                if (updateRequest.getAclId() != null) {
                    updateAcl(updateRequest, link, linkDao);
                }
                if (updateRequest.getFolderId() != null) {
                    updateFolder(updateRequest, link, linkDao);
                }
                if (updateRequest.getObjectId() != null) {
                    updateObject(updateRequest, link, linkDao);
                }
                if (updateRequest.getParentId() != null) {
                    updateParent(updateRequest, link, linkDao);
                }
                if (updateRequest.getOwnerId() != null) {
                    updateOwner(updateRequest, link, linkDao);
                }

                sendGenericResponse(response, new GenericResponse(true));
                return;

            } catch (UpdateException updateException) {
                ErrorResponseGenerator.generateErrorMessage(response, updateException.getStatusCode(), updateException.getErrorCode());
                return;
            }
        } else {
            ErrorResponseGenerator.generateErrorMessage(response, SC_NOT_FOUND,
                    ErrorCode.OBJECT_NOT_FOUND);
            return;
        }

    }

    private void updateFolder(LinkUpdateRequest updateRequest, Link link, LinkDao linkDao) {
        if (link.getObjectId() != null) {
            // link can point either to folder OR object.
            link.setObjectId(null);
            link.setType(LinkType.FOLDER);
        }
        Folder       folder           = new FolderDao().getFolderById(updateRequest.getFolderId()).orElseThrow(() -> FOLDER_NOT_FOUND);
        UserAccount  user             = ThreadLocalSqlSession.getCurrentUser();
        AccessFilter accessFilter     = AccessFilter.getInstance(user);
        boolean      browsePermission = accessFilter.hasPermissionOnOwnable(folder, DefaultPermission.BROWSE_FOLDER, folder);
        if (!browsePermission) {
            throw NO_BROWSE_PERMISSION;
        }
        link.setFolderId(updateRequest.getFolderId());
        if (linkDao.updateLink(link) != UPDATED_ONE_ROW) {
            log.debug("Folder update did not change the link.");
        }
    }

    private void updateParent(LinkUpdateRequest updateRequest, Link link, LinkDao linkDao) {
        Folder       parentFolder     = new FolderDao().getFolderById(updateRequest.getParentId()).orElseThrow(() -> FOLDER_NOT_FOUND);
        UserAccount  user             = ThreadLocalSqlSession.getCurrentUser();
        AccessFilter accessFilter     = AccessFilter.getInstance(user);
        boolean      browsePermission = accessFilter.hasPermissionOnOwnable(parentFolder, DefaultPermission.BROWSE_FOLDER, parentFolder);
        if (!browsePermission) {
            throw NO_BROWSE_PERMISSION;
        }
        boolean writePermission = accessFilter.hasPermissionOnOwnable(parentFolder, DefaultPermission.CREATE_OBJECT, parentFolder);
        if (!writePermission) {
            throw NO_CREATE_PERMISSION;
        }
        link.setParentId(parentFolder.getParentId());
        if (linkDao.updateLink(link) != UPDATED_ONE_ROW) {
            log.debug("Folder parent update did not change the link.");
        }
    }

    private void updateObject(LinkUpdateRequest updateRequest, Link link, LinkDao linkDao) {
        if (link.getFolderId() != null) {
            link.setFolderId(null);
            link.setType(LinkType.OBJECT);
        }
        ObjectSystemData osd       = new OsdDao().getObjectById(updateRequest.getObjectId()).orElseThrow(() -> OBJECT_NOT_FOUND);
        boolean          mayBrowse = AccessFilter.getInstance(ThreadLocalSqlSession.getCurrentUser()).hasPermissionOnOwnable(osd, DefaultPermission.BROWSE_OBJECT, osd);
        if (!mayBrowse) {
            throw NO_BROWSE_PERMISSION;
        }
        link.setObjectId(updateRequest.getObjectId());
        if (linkDao.updateLink(link) != UPDATED_ONE_ROW) {
            log.debug("OSD update did not change the link.");
        }
    }

    private void updateOwner(LinkUpdateRequest updateRequest, Link link, LinkDao linkDao) {
        Optional<UserAccount> ownerOpt = new UserAccountDao().getUserAccountById(updateRequest.getOwnerId());
        link.setOwnerId(ownerOpt.orElseThrow(() -> USER_NOT_FOUND).getId());
        if (linkDao.updateLink(link) != UPDATED_ONE_ROW) {
            log.debug("update owner did not change link");
        }
    }

    private void updateAcl(LinkUpdateRequest updateRequest, Link link, LinkDao linkDao) {
        Acl acl = new AclDao().getAclById(updateRequest.getAclId());
        if (acl == null) {
            ErrorCode.ACL_NOT_FOUND.throwUp();
        }
        UserAccount  user         = ThreadLocalSqlSession.getCurrentUser();
        AccessFilter accessFilter = AccessFilter.getInstance(user);
        if (accessFilter.hasPermissionOnOwnable(link, DefaultPermission.SET_ACL.getName(), link)) {
            link.setAclId(updateRequest.getAclId());
            if (linkDao.updateLink(link) != 1) {
                log.debug("acl update on {} did change anything.", link);
            }
        } else {
            throw MISSING_SET_ACL_PERMISSION;
        }
    }


    private void sendGenericResponse(HttpServletResponse response, GenericResponse genericResponse) throws IOException {
        response.setContentType(CONTENT_TYPE_XML);
        response.setStatus(HttpServletResponse.SC_OK);
        xmlMapper.writeValue(response.getWriter(), genericResponse);
    }

    private void deleteLink(HttpServletRequest request, HttpServletResponse response) throws IOException {
        DeleteByIdRequest deleteRequest = xmlMapper.readValue(request.getInputStream(), DeleteByIdRequest.class);
        if (!deleteRequest.validated()) {
            ErrorResponseGenerator.generateErrorMessage(response, SC_BAD_REQUEST,
                    ErrorCode.ID_PARAM_IS_INVALID, "Id must be a positive integer value.");
            return;
        }
        LinkDao          linkDao          = new LinkDao();
        Optional<Link>   linkOptional     = linkDao.getLinkById(deleteRequest.getId());
        DeletionResponse deletionResponse = new DeletionResponse();
        if (linkOptional.isPresent()) {
            Link        link         = linkOptional.get();
            UserAccount user         = ThreadLocalSqlSession.getCurrentUser();
            List<Link>  filteredLink = authorizationService.filterLinksByBrowsePermission(Collections.singletonList(link), user);
            if (filteredLink.isEmpty()) {
                ErrorResponseGenerator.generateErrorMessage(response, SC_UNAUTHORIZED, ErrorCode.UNAUTHORIZED, "");
                return;
            }

            boolean deleteOkay;
            long    aclId = link.getAclId();
            switch (link.getType()) {
                case FOLDER:
                    String deleteFolderPerm = DefaultPermission.DELETE_FOLDER.getName();
                    deleteOkay = authorizationService.userHasPermission(aclId, deleteFolderPerm, user)
                            ||
                            (link.getOwnerId().equals(user.getId())
                                    && authorizationService.userHasOwnerPermission(aclId, deleteFolderPerm, user))
                    ;
                    break;
                case OBJECT:
                    String deleteObjectPerm = DefaultPermission.DELETE_OBJECT.getName();
                    deleteOkay = authorizationService.userHasPermission(aclId, deleteObjectPerm, user)
                            ||
                            (link.getOwnerId().equals(user.getId())
                                    && authorizationService.userHasOwnerPermission(aclId, deleteObjectPerm, user))
                    ;
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
        } else {
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
        LinkDao        linkDao      = new LinkDao();
        Optional<Link> linkOptional = linkDao.getLinkById(linkRequest.getId());
        if (!linkOptional.isPresent()) {
            ErrorResponseGenerator.generateErrorMessage(response, SC_NOT_FOUND, ErrorCode.OBJECT_NOT_FOUND, "");
            return;
        }

        boolean includeSummary = linkRequest.isIncludeSummary();
        Link    link           = linkOptional.get();

        // check the Link's ACL:
        UserAccount user         = ThreadLocalSqlSession.getCurrentUser();
        List<Link>  filteredLink = authorizationService.filterLinksByBrowsePermission(Collections.singletonList(link), user);
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
        FolderDao    folderDao = new FolderDao();
        List<Folder> folders   = folderDao.getFoldersById(Collections.singletonList(link.getFolderId()), includeSummary);
        // existence of folder should be guaranteed by foreign key constraing in DB.
        Folder       folder       = folders.get(0);
        AccessFilter accessFilter = AccessFilter.getInstance(ThreadLocalSqlSession.getCurrentUser());
        // TODO: check browse permission of owner (#57)
        if (accessFilter.hasFolderBrowsePermission(folder.getAclId())) {
            LinkResponse linkResponse = new LinkResponse();
            linkResponse.setLinkType(LinkType.FOLDER);
            linkResponse.setFolder(folder);
            return Optional.of(linkResponse);
        }
        ErrorResponseGenerator.generateErrorMessage(response, SC_UNAUTHORIZED, ErrorCode.UNAUTHORIZED, "");
        return Optional.empty();
    }

    private Optional<LinkResponse> handleOsdLink(HttpServletResponse response, Link link, boolean includeSummary) {
        OsdDao                 osdDao = new OsdDao();
        List<ObjectSystemData> osds   = osdDao.getObjectsById(Collections.singletonList(link.getObjectId()), includeSummary);
        ObjectSystemData       osd    = osds.get(0);

        AccessFilter accessFilter = AccessFilter.getInstance(ThreadLocalSqlSession.getCurrentUser());
        // TODO: check browse permission of owner (#57)
        if (accessFilter.hasUserBrowsePermission(osd.getAclId())) {
            LinkResponse linkResponse = new LinkResponse();
            linkResponse.setLinkType(LinkType.OBJECT);
            linkResponse.setOsd(osd);
            return Optional.of(linkResponse);

        }
        ErrorResponseGenerator.generateErrorMessage(response, SC_UNAUTHORIZED, ErrorCode.UNAUTHORIZED, "");
        return Optional.empty();
    }

}
