package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.DefaultPermission;
import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.CinnamonRequest;
import com.dewarim.cinnamon.application.CinnamonResponse;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.application.service.DeleteLinkService;
import com.dewarim.cinnamon.dao.*;
import com.dewarim.cinnamon.model.Acl;
import com.dewarim.cinnamon.model.Folder;
import com.dewarim.cinnamon.model.ObjectSystemData;
import com.dewarim.cinnamon.model.UserAccount;
import com.dewarim.cinnamon.model.links.Link;
import com.dewarim.cinnamon.model.links.LinkResolver;
import com.dewarim.cinnamon.model.links.LinkType;
import com.dewarim.cinnamon.model.request.UpdateRequest;
import com.dewarim.cinnamon.model.request.link.CreateLinkRequest;
import com.dewarim.cinnamon.model.request.link.DeleteLinkRequest;
import com.dewarim.cinnamon.model.request.link.GetLinksRequest;
import com.dewarim.cinnamon.model.request.link.UpdateLinkRequest;
import com.dewarim.cinnamon.model.response.LinkResponse;
import com.dewarim.cinnamon.model.response.LinkResponseWrapper;
import com.dewarim.cinnamon.model.response.LinkWrapper;
import com.dewarim.cinnamon.security.authorization.AccessFilter;
import com.dewarim.cinnamon.security.authorization.AuthorizationService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.dewarim.cinnamon.model.links.LinkType.FOLDER;

@WebServlet(name = "Link", urlPatterns = "/")
public class LinkServlet extends HttpServlet implements CruddyServlet<Link> {

    private static final Logger log             = LogManager.getLogger(LinkServlet.class);
    private static final int    UPDATED_ONE_ROW = 1;

    private final DeleteLinkService    deleteLinkService    = new DeleteLinkService();
    private final AuthorizationService authorizationService = new AuthorizationService();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        CinnamonRequest  cinnamonRequest  = (CinnamonRequest) request;
        CinnamonResponse cinnamonResponse = (CinnamonResponse) response;
        LinkDao          linkDao          = new LinkDao();

        UrlMapping mapping = UrlMapping.getByPath(request.getRequestURI());
        switch (mapping) {
            case LINK__CREATE -> create(cinnamonRequest, linkDao, cinnamonResponse);
            case LINK__DELETE -> delete(cinnamonRequest, linkDao, cinnamonResponse);
            case LINK__GET_LINKS_BY_ID -> getLinksById(cinnamonRequest, linkDao, cinnamonResponse);
            case LINK__UPDATE -> update(cinnamonRequest, linkDao, cinnamonResponse);
            default -> ErrorCode.RESOURCE_NOT_FOUND.throwUp();
        }
    }

    private void create(CinnamonRequest request, LinkDao linkDao, CinnamonResponse response) throws IOException {
        CreateLinkRequest linkRequest = (CreateLinkRequest) request.getMapper().readValue(request.getInputStream(), CreateLinkRequest.class)
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
        if (link.getResolver() == null) {
            link.setResolver(LinkResolver.FIXED);
        }
        Folder       parentFolder     = parentFolders.getFirst();
        AccessFilter accessFilter     = AccessFilter.getInstance(user);
        boolean      browsePermission = accessFilter.hasPermissionOnOwnable(parentFolder, DefaultPermission.BROWSE, parentFolder);
        boolean      writePermission  = accessFilter.hasPermissionOnOwnable(parentFolder, DefaultPermission.CREATE_OBJECT, parentFolder);
        if (!(browsePermission && writePermission)) {
            ErrorCode.UNAUTHORIZED.throwUp();
        }

        AclDao        aclDao = new AclDao();
        Optional<Acl> acl    = aclDao.getObjectById(link.getAclId());
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
                    accessFilter.verifyHasPermissionOnOwnable(folder, DefaultPermission.BROWSE, folder, ErrorCode.UNAUTHORIZED);
                } else {
                    ErrorCode.FOLDER_NOT_FOUND.throwUp();
                }
            }
            case OBJECT -> {
                Optional<ObjectSystemData> osdOpt = osdDao.getObjectById(link.getObjectId());
                osd = osdOpt.orElseThrow(ErrorCode.OBJECT_NOT_FOUND.getException());
                accessFilter.verifyHasPermissionOnOwnable(osd, DefaultPermission.BROWSE, osd, ErrorCode.UNAUTHORIZED);
            }
            default -> throw new IllegalStateException("invalid link type: " + link.getType());
        }

        return linkDao.create(Collections.singletonList(link)).getFirst();
    }

    private void update(CinnamonRequest request, LinkDao linkDao, CinnamonResponse response) throws IOException {
        UpdateRequest<Link> updateRequest = request.getMapper().readValue(request.getInputStream(), UpdateLinkRequest.class)
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
        if (update.getResolver() != null) {
            link.setResolver(update.getResolver());
        }
        // no update allowed for links that the user cannot even see:
        accessFilter.verifyHasPermissionOnOwnable(link, DefaultPermission.BROWSE, link, ErrorCode.NO_BROWSE_PERMISSION);

        if (!Objects.equals(link.getAclId(), update.getAclId())) {
            updateAcl(update, link, linkDao, accessFilter);
        }
        if (link.getType() == FOLDER && !Objects.equals(link.getFolderId(), update.getFolderId())) {
            updateFolder(update, link, linkDao, accessFilter);
        } else if (!Objects.equals(link.getObjectId(), update.getObjectId())) {
            updateObject(update, link, linkDao, accessFilter);
        }
        if (!Objects.equals(link.getParentId(), update.getParentId())) {
            updateParent(update, link, linkDao, accessFilter);
        }
        if (!Objects.equals(link.getOwnerId(), update.getOwnerId())) {
            updateOwner(update, link, linkDao, accessFilter);
        }
        return link;
    }

    private void updateFolder(Link updateRequest, Link link, LinkDao linkDao, AccessFilter accessFilter) {
        Folder folder = new FolderDao().getFolderById(updateRequest.getFolderId()).orElseThrow(ErrorCode.FOLDER_NOT_FOUND.getException());
        accessFilter.verifyHasPermissionOnOwnable(folder, DefaultPermission.BROWSE, folder, ErrorCode.NO_BROWSE_PERMISSION);
        accessFilter.verifyHasPermissionOnOwnable(link, DefaultPermission.SET_LINK_TARGET, link, ErrorCode.NO_SET_LINK_TARGET_PERMISSION);
        link.setFolderId(updateRequest.getFolderId());
        if (linkDao.updateLink(link) != UPDATED_ONE_ROW) {
            log.debug("Folder update did not change the link.");
        }
    }

    private void updateParent(Link updateRequest, Link link, LinkDao linkDao, AccessFilter accessFilter) {
        Folder parentFolder = new FolderDao().getFolderById(updateRequest.getParentId()).orElseThrow(ErrorCode.FOLDER_NOT_FOUND.getException());
        accessFilter.verifyHasPermissionOnOwnable(parentFolder, DefaultPermission.BROWSE, parentFolder, ErrorCode.NO_BROWSE_PERMISSION);
        accessFilter.verifyHasPermissionOnOwnable(parentFolder, DefaultPermission.CREATE_OBJECT, parentFolder, ErrorCode.NO_CREATE_PERMISSION);
        accessFilter.verifyHasPermissionOnOwnable(link, DefaultPermission.SET_PARENT, link, ErrorCode.NO_SET_PARENT_PERMISSION);
        link.setParentId(parentFolder.getId());
        if (linkDao.updateLink(link) != UPDATED_ONE_ROW) {
            log.debug("Folder parent update did not change the link.");
        }
    }

    private void updateObject(Link updateRequest, Link link, LinkDao linkDao, AccessFilter accessFilter) {
        ObjectSystemData osd = new OsdDao().getObjectById(updateRequest.getObjectId()).orElseThrow(ErrorCode.OBJECT_NOT_FOUND.getException());
        accessFilter.verifyHasPermissionOnOwnable(osd, DefaultPermission.BROWSE, osd, ErrorCode.NO_BROWSE_PERMISSION);
        accessFilter.verifyHasPermissionOnOwnable(link, DefaultPermission.SET_LINK_TARGET, osd, ErrorCode.NO_SET_LINK_TARGET_PERMISSION);
        link.setObjectId(updateRequest.getObjectId());
        if (linkDao.updateLink(link) != UPDATED_ONE_ROW) {
            log.debug("OSD update did not change the link.");
        }
    }

    private void updateOwner(Link updateRequest, Link link, LinkDao linkDao, AccessFilter accessFilter) {
        Optional<UserAccount> ownerOpt = new UserAccountDao().getUserAccountById(updateRequest.getOwnerId());
        link.setOwnerId(ownerOpt.orElseThrow(ErrorCode.OWNER_NOT_FOUND.getException()).getId());
        accessFilter.verifyHasPermissionOnOwnable(link, DefaultPermission.SET_OWNER, link, ErrorCode.NO_SET_OWNER_PERMISSION);
        if (linkDao.updateLink(link) != UPDATED_ONE_ROW) {
            log.debug("update owner did not change link");
        }
    }

    private void updateAcl(Link updateRequest, Link link, LinkDao linkDao, AccessFilter accessFilter) {
        Acl acl = new AclDao().getObjectById(updateRequest.getAclId()).orElseThrow(ErrorCode.ACL_NOT_FOUND.getException());
        accessFilter.verifyHasPermissionOnOwnable(link, DefaultPermission.SET_ACL, link, ErrorCode.MISSING_SET_ACL_PERMISSION);
        link.setAclId(acl.getId());
        if (linkDao.updateLink(link) != 1) {
            log.debug("acl update on {} did change anything.", link);
        }
    }

    private void delete(CinnamonRequest request, LinkDao linkDao, CinnamonResponse response) throws IOException {
        DeleteLinkRequest deleteRequest = (DeleteLinkRequest) request.getMapper().readValue(request.getInputStream(), DeleteLinkRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());
        List<Link> links = linkDao.getObjectsById(deleteRequest.list());
        if (links.isEmpty() || links.size() != deleteRequest.list().size()) {
            ErrorCode.OBJECT_NOT_FOUND.throwUp();
        }
        UserAccount user          = ThreadLocalSqlSession.getCurrentUser();
        List<Link>  filteredLinks = authorizationService.filterLinksByBrowsePermission(links, user);
        if (filteredLinks.isEmpty() || links.size() != filteredLinks.size()) {
            throw ErrorCode.UNAUTHORIZED.exception();
        }
        deleteLinkService.verifyAndDelete(filteredLinks, user, linkDao);
        response.setWrapper(deleteRequest.fetchResponseWrapper());
    }

    private void getLinksById(CinnamonRequest request, LinkDao linkDao, CinnamonResponse response) throws IOException {
        var linkRequest = request.getMapper().readValue(request.getInputStream(), GetLinksRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());

        int        idCount = linkRequest.getIds().size();
        List<Link> links   = linkDao.getObjectsById(linkRequest.getIds());

        if (links.size() != idCount) {
            throw ErrorCode.OBJECT_NOT_FOUND.getException().get();
        }

        // check the ACLs of all links.
        UserAccount user                = ThreadLocalSqlSession.getCurrentUser();
        List<Link>  folderLinks         = links.stream().filter(link -> link.getFolderId() != null).toList();
        List<Link>  osdLinks            = links.stream().filter(link -> link.getObjectId() != null).toList();
        List<Link>  filteredFolderLinks = authorizationService.filterFolderLinksAndTargetsByBrowsePermission(folderLinks, user);
        List<Link>  filteredOsdLinks    = authorizationService.filterOsdLinksAndTargetsByBrowsePermission(osdLinks, user);

        List<Link> filteredLinks = new ArrayList<>(filteredOsdLinks);
        filteredLinks.addAll(filteredFolderLinks);

        if (filteredLinks.size() != idCount) {
            throw ErrorCode.NO_BROWSE_PERMISSION.getException().get();
        }

        List<LinkResponse> linkResponses  = new ArrayList<>();
        boolean            includeSummary = linkRequest.isIncludeSummary();
        filteredLinks.forEach(link -> {
            switch (link.getType()) {
                case FOLDER -> linkResponses.add(handleFolderLink(link, user, includeSummary));
                case OBJECT -> linkResponses.add(handleOsdLink(link, user, includeSummary));
                default -> throw new IllegalStateException("unknown link type");
            }
        });

        LinkResponseWrapper wrapper = new LinkResponseWrapper(linkResponses);
        response.setWrapper(wrapper);
    }

    private LinkResponse handleFolderLink(Link link, UserAccount user, boolean includeSummary) {
        FolderDao    folderDao = new FolderDao();
        List<Folder> folders   = folderDao.getFoldersById(Collections.singletonList(link.getFolderId()), includeSummary);
        // existence of folder should be guaranteed by foreign key constraint in DB.
        Folder folder = folders.getFirst();

        if (authorizationService.hasUserOrOwnerPermission(folder, DefaultPermission.BROWSE, user)) {
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

    private LinkResponse handleOsdLink(Link link, UserAccount user, boolean includeSummary) {
        OsdDao osdDao = new OsdDao();
        Long   osdId  = link.getObjectId();
        if (link.getResolver() == LinkResolver.LATEST_HEAD) {
            ObjectSystemData osd        = osdDao.getObjectById(osdId).orElseThrow(ErrorCode.OBJECT_NOT_FOUND.getException());
            ObjectSystemData latestHead = osdDao.getLatestHeads(List.of(osd)).get(osdId);
            osdId = latestHead.getId();
            link.setResolvedId(osdId);
        }
        List<ObjectSystemData> osds = osdDao.getObjectsById(List.of(osdId), includeSummary);
        ObjectSystemData       osd  = osds.getFirst();

        if (authorizationService.hasUserOrOwnerPermission(link, DefaultPermission.BROWSE, user)) {
            return getLinkResponse(link, osd);
        }
        throw ErrorCode.UNAUTHORIZED.getException().get();
    }

    private static LinkResponse getLinkResponse(Link link, ObjectSystemData osd) {
        LinkResponse linkResponse = new LinkResponse();
        linkResponse.setType(LinkType.OBJECT);
        linkResponse.setOsd(osd);
        linkResponse.setObjectId(osd.getId());
        linkResponse.setAclId(link.getAclId());
        linkResponse.setId(link.getId());
        linkResponse.setOwnerId(link.getOwnerId());
        linkResponse.setParentId(link.getParentId());
        linkResponse.setResolver(link.getResolver());
        linkResponse.setResolvedId(link.resolveLink());
        return linkResponse;
    }


}
