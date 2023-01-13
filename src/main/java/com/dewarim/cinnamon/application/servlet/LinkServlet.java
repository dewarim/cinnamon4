package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.DefaultPermission;
import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.CinnamonResponse;
import com.dewarim.cinnamon.application.ErrorResponseGenerator;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.application.service.DeleteLinkService;
import com.dewarim.cinnamon.dao.AclDao;
import com.dewarim.cinnamon.dao.FolderDao;
import com.dewarim.cinnamon.dao.LinkDao;
import com.dewarim.cinnamon.dao.OsdDao;
import com.dewarim.cinnamon.dao.UserAccountDao;
import com.dewarim.cinnamon.model.Acl;
import com.dewarim.cinnamon.model.Folder;
import com.dewarim.cinnamon.model.ObjectSystemData;
import com.dewarim.cinnamon.model.UserAccount;
import com.dewarim.cinnamon.model.links.Link;
import com.dewarim.cinnamon.model.links.LinkType;
import com.dewarim.cinnamon.model.request.UpdateRequest;
import com.dewarim.cinnamon.model.request.link.CreateLinkRequest;
import com.dewarim.cinnamon.model.request.link.DeleteLinkRequest;
import com.dewarim.cinnamon.model.request.link.GetLinksRequest;
import com.dewarim.cinnamon.model.request.link.LinkWrapper;
import com.dewarim.cinnamon.model.request.link.UpdateLinkRequest;
import com.dewarim.cinnamon.model.response.LinkResponse;
import com.dewarim.cinnamon.model.response.LinkResponseWrapper;
import com.dewarim.cinnamon.security.authorization.AccessFilter;
import com.dewarim.cinnamon.security.authorization.AuthorizationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.dewarim.cinnamon.api.Constants.XML_MAPPER;
import static com.dewarim.cinnamon.model.links.LinkType.FOLDER;

@WebServlet(name = "Link", urlPatterns = "/")
public class LinkServlet extends HttpServlet implements CruddyServlet<Link> {

    private static final Logger log             = LogManager.getLogger(LinkServlet.class);
    private static final int    UPDATED_ONE_ROW = 1;

    private final DeleteLinkService    deleteLinkService    = new DeleteLinkService();
    private final ObjectMapper         xmlMapper            = XML_MAPPER;
    private final AuthorizationService authorizationService = new AuthorizationService();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        CinnamonResponse cinnamonResponse = (CinnamonResponse) response;
        LinkDao          linkDao          = new LinkDao();

        UrlMapping mapping = UrlMapping.getByPath(request.getRequestURI());
        switch (mapping) {
            case LINK__CREATE -> create(request, linkDao, cinnamonResponse);
            case LINK__DELETE -> delete(request, linkDao, cinnamonResponse);
            case LINK__GET_LINKS_BY_ID -> getLinksById(request, linkDao, cinnamonResponse);
            case LINK__UPDATE -> update(request, linkDao, cinnamonResponse);
            default -> ErrorCode.RESOURCE_NOT_FOUND.throwUp();
        }
    }

    private void create(HttpServletRequest request, LinkDao linkDao, CinnamonResponse response) throws IOException {
        CreateLinkRequest linkRequest = (CreateLinkRequest) getMapper().readValue(request.getInputStream(), CreateLinkRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());

        List<Link>  links       = linkRequest.list().stream().map((Link link) -> createLink(link, linkDao)).collect(Collectors.toList());
        LinkWrapper linkWrapper = new LinkWrapper(links);
        response.setWrapper(linkWrapper);

    }

    private Link createLink(Link link, LinkDao linkDao) {
        UserAccount  user          = ThreadLocalSqlSession.getCurrentUser();
        FolderDao    folderDao     = new FolderDao();
        List<Folder> parentFolders = folderDao.getFoldersById(Collections.singletonList(link.getParentId()), false);
        if (parentFolders.isEmpty()) {
            ErrorCode.PARENT_FOLDER_NOT_FOUND.throwUp();
        }

        Folder       parentFolder     = parentFolders.get(0);
        AccessFilter accessFilter     = AccessFilter.getInstance(user);
        boolean      browsePermission = accessFilter.hasPermissionOnOwnable(parentFolder, DefaultPermission.BROWSE_FOLDER, parentFolder);
        boolean      writePermission  = accessFilter.hasPermissionOnOwnable(parentFolder, DefaultPermission.CREATE_OBJECT, parentFolder);
        if (!(browsePermission && writePermission)) {
            ErrorCode.UNAUTHORIZED.throwUp();
        }

        AclDao        aclDao = new AclDao();
        Optional<Acl> acl    = aclDao.getAclById(link.getAclId());
        if (acl.isEmpty()) {
            ErrorCode.ACL_NOT_FOUND.throwUp();
        }

        // check if owner of new link exists:
        UserAccountDao        userDao  = new UserAccountDao();
        Optional<UserAccount> ownerOpt = userDao.getUserAccountById(link.getOwnerId());
        if (ownerOpt.isEmpty()) {
            ErrorCode.OWNER_NOT_FOUND.throwUp();
        }

        Folder           folder;
        ObjectSystemData osd;
        OsdDao           osdDao = new OsdDao();
        switch (link.getType()) {
            case FOLDER -> {
                Optional<Folder> folderOpt = folderDao.getFolderById(link.getFolderId());
                if (folderOpt.isPresent()) {
                    folder = folderOpt.get();
                    accessFilter.verifyHasPermissionOnOwnable(folder, DefaultPermission.BROWSE_FOLDER, folder, ErrorCode.UNAUTHORIZED);
                } else {
                    ErrorCode.FOLDER_NOT_FOUND.throwUp();
                }
            }
            case OBJECT -> {
                Optional<ObjectSystemData> osdOpt = osdDao.getObjectById(link.getObjectId());
                osd = osdOpt.orElseThrow(ErrorCode.OBJECT_NOT_FOUND.getException());
                accessFilter.verifyHasPermissionOnOwnable(osd, DefaultPermission.BROWSE_OBJECT, osd, ErrorCode.UNAUTHORIZED);
            }
            default -> throw new IllegalStateException("invalid link type: " + link.getType());
        }

        return linkDao.create(Collections.singletonList(link)).get(0);
    }

    private void update(HttpServletRequest request, LinkDao linkDao, CinnamonResponse response) throws IOException {
        UpdateRequest<Link> updateRequest = xmlMapper.readValue(request.getInputStream(), UpdateLinkRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());
        List<Link> links       = updateRequest.list().stream().map(link -> updateLink(link, linkDao)).collect(Collectors.toList());
        var        linkWrapper = new LinkWrapper(links);
        response.setWrapper(linkWrapper);
    }

    private Link updateLink(Link update, LinkDao linkDao) {
        Link         link         = linkDao.getLinkById(update.getId()).orElseThrow(ErrorCode.OBJECT_NOT_FOUND.getException());
        UserAccount  user         = ThreadLocalSqlSession.getCurrentUser();
        AccessFilter accessFilter = AccessFilter.getInstance(user);

        if (link.getType() != update.getType()) {
            ErrorCode.CANNOT_CHANGE_LINK_TYPE.throwUp();
        }

        // no update allowed for links that the user cannot even see:
        accessFilter.verifyHasPermissionOnOwnable(link, DefaultPermission.BROWSE_OBJECT, link, ErrorCode.NO_BROWSE_PERMISSION);
        accessFilter.verifyHasPermissionOnOwnable(link, DefaultPermission.WRITE_OBJECT_SYS_METADATA, link, ErrorCode.NO_WRITE_SYS_METADATA_PERMISSION);

        if (!Objects.equals(link.getAclId(), update.getAclId())) {
            updateAcl(update, link, linkDao);
        }
        if (link.getType() == FOLDER && !Objects.equals(link.getFolderId(), update.getFolderId())) {
            updateFolder(update, link, linkDao);
        } else if (!Objects.equals(link.getObjectId(), update.getObjectId())) {
            updateObject(update, link, linkDao);
        }
        if (!Objects.equals(link.getParentId(), update.getParentId())) {
            updateParent(update, link, linkDao);
        }
        if (!Objects.equals(link.getOwnerId(), update.getOwnerId())) {
            updateOwner(update, link, linkDao);
        }
        return link;
    }

    private void updateFolder(Link updateRequest, Link link, LinkDao linkDao) {
        if (link.getObjectId() != null) {
            // link can point either to folder OR object.
            link.setObjectId(null);
            link.setType(FOLDER);
        }
        Folder       folder       = new FolderDao().getFolderById(updateRequest.getFolderId()).orElseThrow(ErrorCode.FOLDER_NOT_FOUND.getException());
        UserAccount  user         = ThreadLocalSqlSession.getCurrentUser();
        AccessFilter accessFilter = AccessFilter.getInstance(user);
        accessFilter.verifyHasPermissionOnOwnable(folder, DefaultPermission.BROWSE_FOLDER, folder, ErrorCode.NO_BROWSE_PERMISSION);
        link.setFolderId(updateRequest.getFolderId());
        if (linkDao.updateLink(link) != UPDATED_ONE_ROW) {
            log.debug("Folder update did not change the link.");
        }
    }

    private void updateParent(Link updateRequest, Link link, LinkDao linkDao) {
        Folder       parentFolder = new FolderDao().getFolderById(updateRequest.getParentId()).orElseThrow(ErrorCode.FOLDER_NOT_FOUND.getException());
        UserAccount  user         = ThreadLocalSqlSession.getCurrentUser();
        AccessFilter accessFilter = AccessFilter.getInstance(user);
        accessFilter.verifyHasPermissionOnOwnable(parentFolder, DefaultPermission.BROWSE_FOLDER, parentFolder, ErrorCode.NO_BROWSE_PERMISSION);
        accessFilter.verifyHasPermissionOnOwnable(parentFolder, DefaultPermission.CREATE_OBJECT, parentFolder, ErrorCode.NO_CREATE_PERMISSION);
        link.setParentId(parentFolder.getId());
        if (linkDao.updateLink(link) != UPDATED_ONE_ROW) {
            log.debug("Folder parent update did not change the link.");
        }
    }

    private void updateObject(Link updateRequest, Link link, LinkDao linkDao) {
        if (link.getFolderId() != null) {
            link.setFolderId(null);
            link.setType(LinkType.OBJECT);
        }
        ObjectSystemData osd = new OsdDao().getObjectById(updateRequest.getObjectId()).orElseThrow(ErrorCode.OBJECT_NOT_FOUND.getException());
        AccessFilter.getInstance(ThreadLocalSqlSession.getCurrentUser()).verifyHasPermissionOnOwnable(osd, DefaultPermission.BROWSE_OBJECT, osd, ErrorCode.NO_BROWSE_PERMISSION);
        link.setObjectId(updateRequest.getObjectId());
        if (linkDao.updateLink(link) != UPDATED_ONE_ROW) {
            log.debug("OSD update did not change the link.");
        }
    }

    private void updateOwner(Link updateRequest, Link link, LinkDao linkDao) {
        Optional<UserAccount> ownerOpt = new UserAccountDao().getUserAccountById(updateRequest.getOwnerId());
        link.setOwnerId(ownerOpt.orElseThrow(ErrorCode.OWNER_NOT_FOUND.getException()).getId());
        if (linkDao.updateLink(link) != UPDATED_ONE_ROW) {
            log.debug("update owner did not change link");
        }
    }

    private void updateAcl(Link updateRequest, Link link, LinkDao linkDao) {
        Acl          acl          = new AclDao().getAclById(updateRequest.getAclId()).orElseThrow(ErrorCode.ACL_NOT_FOUND.getException());
        UserAccount  user         = ThreadLocalSqlSession.getCurrentUser();
        AccessFilter accessFilter = AccessFilter.getInstance(user);
        accessFilter.verifyHasPermissionOnOwnable(link, DefaultPermission.SET_ACL, link, ErrorCode.MISSING_SET_ACL_PERMISSION);
        link.setAclId(acl.getId());
        if (linkDao.updateLink(link) != 1) {
            log.debug("acl update on {} did change anything.", link);
        }
    }

    private void delete(HttpServletRequest request, LinkDao linkDao, CinnamonResponse response) throws IOException {
        DeleteLinkRequest deleteRequest = (DeleteLinkRequest) xmlMapper.readValue(request.getInputStream(), DeleteLinkRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());
        List<Link> links = linkDao.getObjectsById(deleteRequest.list());
        if (links.isEmpty()) {
            ErrorCode.OBJECT_NOT_FOUND.throwUp();
        }
        UserAccount user          = ThreadLocalSqlSession.getCurrentUser();
        List<Link>  filteredLinks = authorizationService.filterLinksByBrowsePermission(links, user);
        if (filteredLinks.isEmpty() || links.size() != filteredLinks.size()) {
            ErrorCode.UNAUTHORIZED.throwUp();
            ErrorResponseGenerator.generateErrorMessage(response, ErrorCode.UNAUTHORIZED);
            return;
        }
        deleteLinkService.verifyAndDelete(filteredLinks, user, linkDao);
        response.setWrapper(deleteRequest.fetchResponseWrapper());
    }

    private void getLinksById(HttpServletRequest request, LinkDao linkDao, CinnamonResponse response) throws IOException {
        var linkRequest = (GetLinksRequest) xmlMapper.readValue(request.getInputStream(), GetLinksRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());

        int        idCount = linkRequest.getIds().size();
        List<Link> links   = linkDao.getObjectsById(linkRequest.getIds());

        if (links.size() != idCount) {
            throw ErrorCode.OBJECT_NOT_FOUND.getException().get();
        }

        // check the ACLs of all links.
        UserAccount user          = ThreadLocalSqlSession.getCurrentUser();
        List<Link>  filteredLinks = authorizationService.filterLinksByBrowsePermission(links, user);
        if (filteredLinks.size() != idCount) {
            throw ErrorCode.NO_BROWSE_PERMISSION.getException().get();
        }

        List<LinkResponse> linkResponses  = new ArrayList<>();
        boolean            includeSummary = linkRequest.isIncludeSummary();
        filteredLinks.forEach(link -> {
            switch (link.getType()) {
                case FOLDER -> linkResponses.add(handleFolderLink(link, includeSummary));
                case OBJECT -> linkResponses.add(handleOsdLink(link, includeSummary));
                default -> throw new IllegalStateException("unknown link type");
            }
        });

        LinkResponseWrapper wrapper = new LinkResponseWrapper(linkResponses);
        response.setWrapper(wrapper);
    }

    private LinkResponse handleFolderLink(Link link, boolean includeSummary) {
        FolderDao    folderDao = new FolderDao();
        List<Folder> folders   = folderDao.getFoldersById(Collections.singletonList(link.getFolderId()), includeSummary);
        // existence of folder should be guaranteed by foreign key constraint in DB.
        Folder       folder       = folders.get(0);
        AccessFilter accessFilter = AccessFilter.getInstance(ThreadLocalSqlSession.getCurrentUser());
        // TODO: check browse permission of owner (#57)
        if (accessFilter.hasFolderBrowsePermission(folder.getAclId())) {
            LinkResponse linkResponse = new LinkResponse();
            linkResponse.setType(LinkType.FOLDER);
            linkResponse.setFolder(folder);
            linkResponse.setFolderId(folder.getId());
            linkResponse.setAclId(link.getAclId());
            linkResponse.setId(link.getId());
            linkResponse.setOwnerId(link.getOwnerId());
            linkResponse.setParentId(link.getParentId());
            return linkResponse;
        }
        throw ErrorCode.UNAUTHORIZED.getException().get();
    }

    private LinkResponse handleOsdLink(Link link, boolean includeSummary) {
        OsdDao                 osdDao = new OsdDao();
        List<ObjectSystemData> osds   = osdDao.getObjectsById(Collections.singletonList(link.getObjectId()), includeSummary);
        ObjectSystemData       osd    = osds.get(0);

        AccessFilter accessFilter = AccessFilter.getInstance(ThreadLocalSqlSession.getCurrentUser());
        // TODO: check browse permission of owner (#57)
        if (accessFilter.hasUserBrowsePermission(osd.getAclId())) {
            LinkResponse linkResponse = new LinkResponse();
            linkResponse.setType(LinkType.OBJECT);
            linkResponse.setOsd(osd);
            linkResponse.setObjectId(osd.getId());
            linkResponse.setAclId(link.getAclId());
            linkResponse.setId(link.getId());
            linkResponse.setOwnerId(link.getOwnerId());
            linkResponse.setParentId(link.getParentId());
            return linkResponse;
        }
        throw ErrorCode.UNAUTHORIZED.getException().get();
    }

    @Override
    public ObjectMapper getMapper() {
        return xmlMapper;
    }
}
