package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.DefaultPermission;
import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.Constants;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.CinnamonResponse;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.application.exception.BadArgumentException;
import com.dewarim.cinnamon.application.service.DeleteLinkService;
import com.dewarim.cinnamon.application.service.DeleteOsdService;
import com.dewarim.cinnamon.application.service.MetaService;
import com.dewarim.cinnamon.dao.AclDao;
import com.dewarim.cinnamon.dao.FolderDao;
import com.dewarim.cinnamon.dao.FolderMetaDao;
import com.dewarim.cinnamon.dao.FolderTypeDao;
import com.dewarim.cinnamon.dao.LinkDao;
import com.dewarim.cinnamon.dao.OsdDao;
import com.dewarim.cinnamon.dao.UserAccountDao;
import com.dewarim.cinnamon.model.Acl;
import com.dewarim.cinnamon.model.Folder;
import com.dewarim.cinnamon.model.FolderType;
import com.dewarim.cinnamon.model.Meta;
import com.dewarim.cinnamon.model.ObjectSystemData;
import com.dewarim.cinnamon.model.UserAccount;
import com.dewarim.cinnamon.model.links.Link;
import com.dewarim.cinnamon.model.request.CreateMetaRequest;
import com.dewarim.cinnamon.model.request.CreateRequest;
import com.dewarim.cinnamon.model.request.DeleteAllMetasRequest;
import com.dewarim.cinnamon.model.request.DeleteMetaRequest;
import com.dewarim.cinnamon.model.request.IdListRequest;
import com.dewarim.cinnamon.model.request.MetaRequest;
import com.dewarim.cinnamon.model.request.SetSummaryRequest;
import com.dewarim.cinnamon.model.request.folder.CreateFolderRequest;
import com.dewarim.cinnamon.model.request.folder.DeleteFolderRequest;
import com.dewarim.cinnamon.model.request.folder.FolderPathRequest;
import com.dewarim.cinnamon.model.request.folder.FolderRequest;
import com.dewarim.cinnamon.model.request.folder.SingleFolderRequest;
import com.dewarim.cinnamon.model.request.folder.UpdateFolderRequest;
import com.dewarim.cinnamon.model.request.osd.VersionPredicate;
import com.dewarim.cinnamon.model.response.DeleteResponse;
import com.dewarim.cinnamon.model.response.FolderWrapper;
import com.dewarim.cinnamon.model.response.Summary;
import com.dewarim.cinnamon.model.response.SummaryWrapper;
import com.dewarim.cinnamon.security.authorization.AccessFilter;
import com.dewarim.cinnamon.security.authorization.AuthorizationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.dewarim.cinnamon.api.Constants.XML_MAPPER;

@WebServlet(name = "Folder", urlPatterns = "/")
public class FolderServlet extends BaseServlet implements CruddyServlet<Folder> {

    private final ObjectMapper         xmlMapper            = XML_MAPPER;
    private final AuthorizationService authorizationService = new AuthorizationService();
    private final DeleteOsdService     deleteOsdService     = new DeleteOsdService();
    private final DeleteLinkService    deleteLinkService    = new DeleteLinkService();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        UserAccount      user             = ThreadLocalSqlSession.getCurrentUser();
        FolderDao        folderDao        = new FolderDao();
        CinnamonResponse cinnamonResponse = (CinnamonResponse) response;
        UrlMapping       mapping          = UrlMapping.getByPath(request.getRequestURI());

        switch (mapping) {
            case FOLDER__CREATE -> createFolder(request, cinnamonResponse, user, folderDao);
            case FOLDER__CREATE_META -> createMeta(request, cinnamonResponse, user, folderDao);
            case FOLDER__DELETE -> delete(request, cinnamonResponse, user, folderDao);
            case FOLDER__DELETE_META -> deleteMeta(request, cinnamonResponse, user, folderDao);
            case FOLDER__DELETE_ALL_METAS -> deleteAllMetas(request, cinnamonResponse, user, folderDao);
            case FOLDER__GET_FOLDER -> getFolder(request, cinnamonResponse, user, folderDao);
            case FOLDER__GET_FOLDER_BY_PATH -> getFolderByPath(request, cinnamonResponse, user, folderDao);
            case FOLDER__GET_FOLDERS -> getFolders(request, cinnamonResponse, user, folderDao);
            case FOLDER__GET_META -> getMeta(request, cinnamonResponse, user, folderDao);
            case FOLDER__GET_SUBFOLDERS -> getSubFolders(request, cinnamonResponse, user, folderDao);
            case FOLDER__SET_SUMMARY -> setSummary(request, cinnamonResponse, user, folderDao);
            case FOLDER__GET_SUMMARIES -> getSummaries(request, cinnamonResponse, user, folderDao);
            case FOLDER__UPDATE -> updateFolder(request, cinnamonResponse, user, folderDao);
            default -> ErrorCode.RESOURCE_NOT_FOUND.throwUp();
        }
    }

    private void deleteAllMetas(HttpServletRequest request, CinnamonResponse cinnamonResponse, UserAccount user, FolderDao folderDao) throws IOException {
        DeleteAllMetasRequest metaRequest = (DeleteAllMetasRequest) getMapper().readValue(request.getInputStream(), DeleteAllMetasRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());

        FolderMetaDao metaDao = new FolderMetaDao();
        List<Long> metaIds = metaDao.listByObjectIds(metaRequest.getIds()).stream().map(Meta::getId).collect(Collectors.toList());
        new MetaService<>().deleteMeta(metaDao, metaIds, folderDao, user );
        cinnamonResponse.setWrapper(new DeleteResponse(true));
    }

    private void delete(HttpServletRequest request, CinnamonResponse cinnamonResponse, UserAccount user, FolderDao folderDao) throws IOException {
        DeleteFolderRequest deleteRequest = xmlMapper.readValue(request.getInputStream(), DeleteFolderRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());
        boolean deleteRecursively = deleteRequest.isDeleteRecursively();
        boolean deleteContent     = deleteRequest.isDeleteContent();
        // load folders recursively (if needed)
        List<Folder> folders = loadFolders(deleteRequest.getIds(), deleteRecursively, deleteContent, folderDao);

        for (Folder folder : folders) {
            authorizationService.throwUpUnlessUserOrOwnerHasPermission(folder, DefaultPermission.DELETE_FOLDER, user, ErrorCode.NO_DELETE_PERMISSION);
        }

        List<Long> folderIds = folders.stream().map(Folder::getId).collect(Collectors.toList());
        OsdDao     osdDao    = new OsdDao();
        if (deleteContent) {
            for (Folder folder : folders) {
                List<ObjectSystemData> osds = osdDao.getObjectsByFolderId(folder.getId(), false, VersionPredicate.ALL);
                deleteOsdService.verifyAndDelete(osds, true, true, user);
            }
        } else if (folderDao.hasContent(folderIds)) {
            throw ErrorCode.FOLDER_IS_NOT_EMPTY.exception();
        }

        if (folderDao.hasSubfolders(folderIds) && !deleteRecursively) {
            throw ErrorCode.FOLDER_HAS_SUBFOLDERS.exception();
        }

        LinkDao    linkDao = new LinkDao();
        List<Link> links   = linkDao.getLinksToOutsideStuff(folderIds);
        deleteLinkService.verifyAndDelete(links, user, linkDao);

        // delete links from folders to inside folders / objects
        linkDao.deleteAllLinksToFolders(folderIds);

        // delete metadata
        FolderMetaDao metaDao = new FolderMetaDao();
        metaDao.deleteByFolderIds(folderIds);

        folderDao.delete(folderIds);

        var deleteResponse = new DeleteResponse(true);
        cinnamonResponse.setWrapper(deleteResponse);
    }

    private List<Folder> loadFolders(List<Long> ids, boolean recursively, boolean deleteContent, FolderDao folderDao) {
        List<Folder> folders = folderDao.getFoldersById(ids, false);
        if (folders.size() != ids.size()) {
            throw ErrorCode.FOLDER_NOT_FOUND.getException().get();
        }
        checkFoldersForContent(ids, deleteContent, folderDao);
        List<Folder> foldersWithSubfolders = new ArrayList<>(folders);
        if (recursively) {
            for (Folder folder : folders) {
                List<Folder> subFolders = folderDao.getDirectSubFolders(folder.getId(), false);
                if (subFolders.size() > 0) {
                    checkFoldersForContent(ids, deleteContent, folderDao);
                    foldersWithSubfolders.addAll(subFolders);
                    foldersWithSubfolders.addAll(loadFolders(subFolders.stream().map(Folder::getId).collect(Collectors.toList()), true, deleteContent, folderDao));
                }
            }
        }
        return foldersWithSubfolders;
    }

    private void checkFoldersForContent(List<Long> ids, boolean deleteContent, FolderDao folderDao) {
        if (!deleteContent && folderDao.hasContent(ids)) {
            throw ErrorCode.FOLDER_IS_NOT_EMPTY.getException().get();
        }
    }

    private void createFolder(HttpServletRequest request, CinnamonResponse response, UserAccount user, FolderDao folderDao) throws IOException {
        CreateRequest<Folder> createRequest = xmlMapper.readValue(request.getInputStream(), CreateFolderRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());
        List<Folder> folders = new ArrayList<>();

        for (Folder folder : createRequest.list()) {
            Long parentId = folder.getParentId();
            Folder parentFolder = folderDao.getFolderById(parentId)
                    .orElseThrow(ErrorCode.PARENT_FOLDER_NOT_FOUND.getException());

            AccessFilter accessFilter = AccessFilter.getInstance(user);
            if (!accessFilter.hasPermissionOnOwnable(parentFolder, DefaultPermission.CREATE_FOLDER, parentFolder)) {
                ErrorCode.NO_CREATE_PERMISSION.throwUp();
            }
            String name = folder.getName();
            folderDao.getFolderByParentAndName(parentFolder.getId(), name, false)
                    .ifPresent(f -> ErrorCode.DUPLICATE_FOLDER_NAME_FORBIDDEN.throwUp());

            FolderTypeDao typeDao = new FolderTypeDao();
            Long          typeId  = folder.getTypeId();
            if (typeId == null) {
                typeId = typeDao.getFolderTypeByName(Constants.FOLDER_TYPE_DEFAULT)
                        .orElseThrow(ErrorCode.FOLDER_TYPE_NOT_FOUND.getException()).getId();
            } else {
                typeId = typeDao.getFolderTypeById(typeId)
                        .orElseThrow(ErrorCode.FOLDER_TYPE_NOT_FOUND.getException()).getId();
            }

            Long ownerId = folder.getOwnerId();
            if (ownerId == null) {
                ownerId = parentFolder.getOwnerId();
            } else {
                ownerId = new UserAccountDao().getUserAccountById(ownerId)
                        .orElseThrow(ErrorCode.USER_ACCOUNT_NOT_FOUND.getException()).getId();
            }

            Long aclId = folder.getAclId();
            if (aclId == null) {
                aclId = parentFolder.getAclId();
            } else {
                aclId = new AclDao().getAclById(aclId)
                        .orElseThrow(ErrorCode.ACL_NOT_FOUND.getException()).getId();
            }

            Folder newFolder = new Folder(name, aclId, ownerId, parentId, typeId, folder.getSummary());
            folders.add(folderDao.saveFolder(newFolder));
        }

        FolderWrapper wrapper = new FolderWrapper(folders);
        response.setWrapper(wrapper);
    }

    private void getSubFolders(HttpServletRequest request, CinnamonResponse response, UserAccount user, FolderDao folderDao) throws IOException {
        SingleFolderRequest folderRequest = xmlMapper.readValue(request.getInputStream(), SingleFolderRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());
        Folder             folder              = folderDao.getFolderById(folderRequest.getId()).orElseThrow(ErrorCode.FOLDER_NOT_FOUND.getException());
        List<Folder>       subFolders          = folderDao.getDirectSubFolders(folder.getId(), folderRequest.isIncludeSummary());
        final AccessFilter accessFilter        = AccessFilter.getInstance(user);
        List<Folder>       browsableSubFolders = subFolders.stream().filter(accessFilter::hasBrowsePermissionForOwnable).collect(Collectors.toList());
        response.setWrapper(new FolderWrapper(browsableSubFolders));
    }

    private void updateFolder(HttpServletRequest request, CinnamonResponse response, UserAccount user, FolderDao folderDao) throws IOException {
        UpdateFolderRequest updateRequest = xmlMapper.readValue(request.getInputStream(), UpdateFolderRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());

        Long         folderId     = updateRequest.getId();
        Folder       folder       = folderDao.getFolderById(folderId, true).orElseThrow(ErrorCode.FOLDER_NOT_FOUND.getException());
        AccessFilter accessFilter = AccessFilter.getInstance(user);

        if (!accessFilter.hasPermissionOnOwnable(folder, DefaultPermission.EDIT_FOLDER, folder)) {
            throw ErrorCode.NO_EDIT_FOLDER_PERMISSION.exception();
        }

        throwUnlessSysMetadataIsWritable(folder);

        boolean changed = false;
        // change parent folder
        Long parentId = updateRequest.getParentId();
        if (parentId != null) {
            if (parentId.equals(folderId)) {
                ErrorCode.CANNOT_MOVE_FOLDER_INTO_ITSELF.throwUp();
            }
            Folder parentFolder = folderDao.getFolderById(parentId)
                    .orElseThrow(ErrorCode.PARENT_FOLDER_NOT_FOUND.getException());
            if (!accessFilter.hasPermissionOnOwnable(parentFolder, DefaultPermission.CREATE_FOLDER, parentFolder)) {
                ErrorCode.NO_CREATE_PERMISSION.throwUp();
            }
            if (!accessFilter.hasPermissionOnOwnable(folder, DefaultPermission.MOVE, folder)) {
                ErrorCode.NO_MOVE_PERMISSION.throwUp();
            }
            folder.setParentId(parentFolder.getId());
            changed = true;
        }

        // change name
        String name = updateRequest.getName();
        if (name != null) {
            Folder parentFolder;
            if(folder.getParentId() == null){
                parentFolder = folderDao.getRootFolder(false);
            }
            else{
                parentFolder = folderDao.getFolderById(folder.getParentId())
                        .orElseThrow(ErrorCode.PARENT_FOLDER_NOT_FOUND.getException());
            }
            // check if name is valid, otherwise user gets a confusing duplicate field db exception:
            folderDao.getFolderByParentAndName(parentFolder.getId(), name, false)
                    .ifPresent(f -> ErrorCode.DUPLICATE_FOLDER_NAME_FORBIDDEN.throwUp());
            folder.setName(name);
            changed = true;
        }

        // change type
        Long typeId = updateRequest.getTypeId();
        if (typeId != null) {
            FolderType type = new FolderTypeDao().getFolderTypeById(typeId)
                    .orElseThrow(ErrorCode.FOLDER_TYPE_NOT_FOUND.getException());
            folder.setTypeId(type.getId());
            changed = true;
        }

        // change acl
        Long aclId = updateRequest.getAclId();
        if (aclId != null) {
            if (!accessFilter.hasPermissionOnOwnable(folder, DefaultPermission.SET_ACL, folder)) {
                ErrorCode.MISSING_SET_ACL_PERMISSION.throwUp();
            }
            Acl acl = new AclDao().getAclById(aclId)
                    .orElseThrow(ErrorCode.ACL_NOT_FOUND.getException());
            folder.setAclId(acl.getId());
            changed = true;
        }

        // change owner
        Long ownerId = updateRequest.getOwnerId();
        if (ownerId != null) {
            UserAccount owner = new UserAccountDao().getUserAccountById(ownerId)
                    .orElseThrow(ErrorCode.USER_ACCOUNT_NOT_FOUND.getException());
            folder.setOwnerId(owner.getId());
            changed = true;
        }

        // update folder:
        if (changed) {
            folderDao.updateFolder(folder);
        }
        response.responseIsGenericOkay();
    }

    private void deleteMeta(HttpServletRequest request, CinnamonResponse response, UserAccount user, FolderDao folderDao) throws IOException {
        DeleteMetaRequest metaRequest = (DeleteMetaRequest) getMapper().readValue(request.getInputStream(), DeleteMetaRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());

        FolderMetaDao metaDao = new FolderMetaDao();
        new MetaService<>().deleteMeta(metaDao, metaRequest.getIds(), folderDao, user );
        var deleteResponse = new DeleteResponse(true);
        response.setWrapper(deleteResponse);
    }

    /**
     * Note: getMeta allows FolderMetaRequests without metaset name to return all metasets.
     * This usage is deprecated.
     * Current Cinnamon 3 clients expect the arbitrary metaset XML to be added to the DOM of the metaset wrapper.
     * This requires assembling a new DOM tree in memory for each request to getMeta, which is not something you
     * want to see with large metasets.
     */
    private void getMeta(HttpServletRequest request, CinnamonResponse response, UserAccount user, FolderDao folderDao) throws IOException {
        MetaRequest metaRequest = xmlMapper.readValue(request.getInputStream(), MetaRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());

        Long   folderId = metaRequest.getId();
        Folder folder   = folderDao.getFolderById(folderId).orElseThrow(ErrorCode.OBJECT_NOT_FOUND.getException());
        throwUnlessCustomMetaIsReadable(folder);

        List<Meta> metaList;
        if (metaRequest.getTypeIds() != null) {
            metaList = new FolderMetaDao().getMetaByTypeIdsAndOsd(metaRequest.getTypeIds(), folderId);
        } else {
            metaList = new FolderMetaDao().listByFolderId(folderId);
        }

        createMetaResponse(response, metaList);
    }

    private void createMeta(HttpServletRequest request, CinnamonResponse response, UserAccount user, FolderDao folderDao) throws IOException {
        CreateMetaRequest metaRequest = xmlMapper.readValue(request.getInputStream(), CreateMetaRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());

        List<Meta> metas = new MetaService<>().createMeta(new FolderMetaDao(), metaRequest.getMetas(), folderDao, user);
        createMetaResponse(response, metas);
    }

    private void getFolderByPath(HttpServletRequest request, CinnamonResponse response, UserAccount user, FolderDao folderDao) throws IOException {
        FolderPathRequest pathRequest = xmlMapper.readValue(request.getInputStream(), FolderPathRequest.class);
        if (pathRequest.validated()) {

            List<Folder> rawFolders;
            try {
                rawFolders = folderDao.getFolderByPathWithAncestors(pathRequest.getPath(), pathRequest.isIncludeSummary());
            } catch (BadArgumentException e) {
                throw e.getErrorCode().exception();
            }
            List<Folder> folders = new AuthorizationService().filterFoldersByBrowsePermission(rawFolders, user);
            if (folders.isEmpty()) {
                throw ErrorCode.OBJECT_NOT_FOUND.exception();
            }
            response.setWrapper(new FolderWrapper(folders));
        } else {
            throw ErrorCode.INVALID_REQUEST.exception();
        }
    }


    /**
     * Retrieve a single folder, including ancestors.
     */
    private void getFolder(HttpServletRequest request, CinnamonResponse response, UserAccount user, FolderDao folderDao) throws IOException {
        SingleFolderRequest folderRequest = xmlMapper.readValue(request.getInputStream(), SingleFolderRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());

        List<Folder> rawFolders = folderDao.getFolderByIdWithAncestors(folderRequest.getId(), folderRequest.isIncludeSummary());
        List<Folder> folders    = new AuthorizationService().filterFoldersByBrowsePermission(rawFolders, user);
        if (folders.isEmpty()) {
            throw ErrorCode.FOLDER_NOT_FOUND.exception();
        }
        response.setWrapper(new FolderWrapper(folders));
    }

    /**
     * Retrieve a list of folders, without including their ancestors.
     */
    private void getFolders(HttpServletRequest request, CinnamonResponse response, UserAccount user, FolderDao folderDao) throws IOException {
        FolderRequest folderRequest = xmlMapper.readValue(request.getInputStream(), FolderRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());

        List<Folder> rawFolders = folderDao.getFoldersById(folderRequest.getIds(), folderRequest.isIncludeSummary());
        List<Folder> folders    = new AuthorizationService().filterFoldersByBrowsePermission(rawFolders, user);
        if (folders.isEmpty()) {
            throw ErrorCode.FOLDER_NOT_FOUND.exception();
        }
        response.setWrapper(new FolderWrapper(folders));
    }

    private void setSummary(HttpServletRequest request, CinnamonResponse response, UserAccount user, FolderDao folderDao) throws IOException {
        // TODO: add SetSummaryRequest.validateRequest
        SetSummaryRequest summaryRequest = xmlMapper.readValue(request.getInputStream(), SetSummaryRequest.class);
        Optional<Folder>  folderOpt      = folderDao.getFolderById(summaryRequest.getId());
        if (folderOpt.isPresent()) {
            Folder folder = folderOpt.get();
            if (authorizationService.hasUserOrOwnerPermission(folder, DefaultPermission.WRITE_OBJECT_SYS_METADATA, user)) {
                folder.setSummary(summaryRequest.getSummary());
                folderDao.updateFolder(folder);
                response.responseIsGenericOkay();
                return;
            } else {
                throw ErrorCode.NO_WRITE_SYS_METADATA_PERMISSION.exception();
            }
        }
        ErrorCode.OBJECT_NOT_FOUND.throwUp();
    }

    private void getSummaries(HttpServletRequest request, CinnamonResponse response, UserAccount user, FolderDao folderDao) throws IOException {
        IdListRequest  idListRequest = xmlMapper.readValue(request.getInputStream(), IdListRequest.class);
        SummaryWrapper wrapper       = new SummaryWrapper();
        List<Folder>   folders       = folderDao.getFoldersById(idListRequest.getIdList(), true);
        folders.forEach(folder -> {
            if (authorizationService.hasUserOrOwnerPermission(folder, DefaultPermission.READ_OBJECT_SYS_METADATA, user)) {
                wrapper.getSummaries().add(new Summary(folder.getId(), folder.getSummary()));
            } else {
                throw ErrorCode.NO_READ_OBJECT_SYS_METADATA_PERMISSION.exception();
            }
        });
        response.setWrapper(wrapper);
    }

    @Override
    public ObjectMapper getMapper() {
        return xmlMapper;
    }
}
