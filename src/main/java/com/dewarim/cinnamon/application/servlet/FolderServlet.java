package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.DefaultPermission;
import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.CinnamonRequest;
import com.dewarim.cinnamon.application.CinnamonResponse;
import com.dewarim.cinnamon.application.RequestScope;
import com.dewarim.cinnamon.application.service.FolderService;
import com.dewarim.cinnamon.application.service.MetaService;
import com.dewarim.cinnamon.dao.FolderDao;
import com.dewarim.cinnamon.dao.FolderMetaDao;
import com.dewarim.cinnamon.dao.LinkDao;
import com.dewarim.cinnamon.dao.UserAccountDao;
import com.dewarim.cinnamon.model.Folder;
import com.dewarim.cinnamon.model.Meta;
import com.dewarim.cinnamon.model.UserAccount;
import com.dewarim.cinnamon.model.links.Link;
import com.dewarim.cinnamon.model.links.LinkType;
import com.dewarim.cinnamon.model.request.CreateRequest;
import com.dewarim.cinnamon.model.request.IdListRequest;
import com.dewarim.cinnamon.model.request.folder.*;
import com.dewarim.cinnamon.model.request.meta.*;
import com.dewarim.cinnamon.model.request.osd.SetSummaryRequest;
import com.dewarim.cinnamon.model.response.*;
import com.dewarim.cinnamon.security.authorization.AccessFilter;
import com.dewarim.cinnamon.security.authorization.AuthorizationService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@WebServlet(name = "Folder", urlPatterns = "/")
public class FolderServlet extends BaseServlet implements CruddyServlet<Folder> {

    private final AuthorizationService authorizationService = new AuthorizationService();
    private final FolderService        folderService        = new FolderService();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        UserAccount      user             = RequestScope.getCurrentUser();
        FolderDao        folderDao        = new FolderDao();
        CinnamonResponse cinnamonResponse = (CinnamonResponse) response;
        CinnamonRequest  cinnamonRequest = (CinnamonRequest) request; 
        UrlMapping       mapping          = UrlMapping.getByPath(request.getRequestURI());

        switch (mapping) {
            case FOLDER__CREATE -> createFolder(cinnamonRequest, cinnamonResponse, user, folderDao);
            case FOLDER__CREATE_META -> createMeta(cinnamonRequest, cinnamonResponse, user, folderDao);
            case FOLDER__DELETE -> delete(cinnamonRequest, cinnamonResponse, user, folderDao);
            case FOLDER__DELETE_META -> deleteMeta(cinnamonRequest, cinnamonResponse, user, folderDao);
            case FOLDER__DELETE_ALL_METAS -> deleteAllMetas(cinnamonRequest, cinnamonResponse, user, folderDao);
            case FOLDER__GET_FOLDER -> getFolder(cinnamonRequest, cinnamonResponse, user, folderDao);
            case FOLDER__GET_FOLDER_BY_PATH -> getFolderByPath(cinnamonRequest, cinnamonResponse, user, folderDao);
            case FOLDER__GET_FOLDER_BY_RELATIVE_PATH ->
                    getFolderByRelativePath(cinnamonRequest, cinnamonResponse, user, folderDao);
            case FOLDER__GET_FOLDERS -> getFolders(cinnamonRequest, cinnamonResponse, user, folderDao);
            case FOLDER__GET_META -> getMeta(cinnamonRequest, cinnamonResponse, folderDao);
            case FOLDER__GET_SUBFOLDERS -> getSubFolders(cinnamonRequest, cinnamonResponse, user, folderDao);
            case FOLDER__SET_SUMMARY -> setSummary(cinnamonRequest, cinnamonResponse, user, folderDao);
            case FOLDER__GET_SUMMARIES -> getSummaries(cinnamonRequest, cinnamonResponse, user, folderDao);
            case FOLDER__UPDATE -> updateFolder(cinnamonRequest, cinnamonResponse, user, folderDao);
            case FOLDER__UPDATE_META_CONTENT -> updateMetaContent(cinnamonRequest, cinnamonResponse, user, folderDao);
            default -> ErrorCode.RESOURCE_NOT_FOUND.throwUp();
        }
    }

    private void updateMetaContent(CinnamonRequest request, CinnamonResponse cinnamonResponse, UserAccount user, FolderDao folderDao) throws IOException {
        UpdateMetaRequest metaRequest = (UpdateMetaRequest) request.getMapper().readValue(request.getInputStream(), UpdateMetaRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());
        FolderMetaDao folderMetaDao = new FolderMetaDao();
        new MetaService<>().updateMeta(folderMetaDao, metaRequest.getMetas(), folderDao, user);
        cinnamonResponse.setResponse(new GenericResponse(true));
    }


    private void deleteAllMetas(CinnamonRequest request, CinnamonResponse cinnamonResponse, UserAccount user, FolderDao folderDao) throws IOException {
        DeleteAllMetasRequest metaRequest = (DeleteAllMetasRequest) request.getMapper().readValue(request.getInputStream(), DeleteAllMetasRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());

        FolderMetaDao metaDao = new FolderMetaDao();
        List<Meta>    metas   = metaDao.listMetaByObjectIds(metaRequest.getIds().stream().toList());
        new MetaService<>().deleteMetas(metaDao, metas, folderDao, user);
        cinnamonResponse.setWrapper(new DeleteResponse(true));
    }

    private void delete(CinnamonRequest request, CinnamonResponse cinnamonResponse, UserAccount user, FolderDao folderDao) throws IOException {
        DeleteFolderRequest deleteRequest = request.getMapper().readValue(request.getInputStream(), DeleteFolderRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());
        folderService.deleteFolder(deleteRequest.getIds(), deleteRequest.isDeleteRecursively(), deleteRequest.isDeleteContent(), user);
        cinnamonResponse.setWrapper(new DeleteResponse(true));
    }

    private void createFolder(CinnamonRequest request, CinnamonResponse response, UserAccount user, FolderDao folderDao) throws IOException {
        CreateRequest<Folder> createRequest = request.getMapper().readValue(request.getInputStream(), CreateFolderRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());
        List<Folder> folders = new ArrayList<>();

        for (Folder folder : createRequest.list()) {
            Long parentId = folder.getParentId();
            Folder parentFolder = folderDao.getFolderById(parentId)
                    .orElseThrow(ErrorCode.PARENT_FOLDER_NOT_FOUND.getException());

            Long ownerId = folder.getOwnerId();
            if (ownerId == null) {
                ownerId = parentFolder.getOwnerId();
            } else {
                ownerId = new UserAccountDao().getUserAccountById(ownerId)
                        .orElseThrow(ErrorCode.USER_ACCOUNT_NOT_FOUND.getException()).getId();
            }

            Folder newFolder = folderService.createFolder(
                    folder.getName(), parentId, folder.getAclId(), folder.getTypeId(),
                    folder.getSummary(), ownerId, user);

            if (folder.getMetasets() != null && !folder.getMetasets().isEmpty()) {
                createMetas(folder.getMetasets(), newFolder.getId());
            }
            folders.add(newFolder);
        }

        response.setWrapper(new FolderWrapper(folders));
    }

    private void getSubFolders(CinnamonRequest request, CinnamonResponse response, UserAccount user, FolderDao folderDao) throws IOException {
        SingleFolderRequest folderRequest = request.getMapper().readValue(request.getInputStream(), SingleFolderRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());
        Folder             folder              = folderDao.getFolderById(folderRequest.getId()).orElseThrow(ErrorCode.FOLDER_NOT_FOUND.getException());
        List<Folder>       subFolders          = folderDao.getDirectSubFolders(folder.getId(), folderRequest.isIncludeSummary());
        final AccessFilter accessFilter        = AccessFilter.getInstance(user);
        List<Folder>       browsableSubFolders = subFolders.stream().filter(accessFilter::hasBrowsePermissionForOwnable).collect(Collectors.toList());
        LinkDao            linkDao             = new LinkDao();
        List<Folder>       parentAndSubFolders = new ArrayList<>();
        parentAndSubFolders.add(folder);
        parentAndSubFolders.addAll(browsableSubFolders);
        List<Long> folderIdsForLinkQuery = parentAndSubFolders.stream().map(Folder::getId).toList();

        FolderWrapper folderWrapper = new FolderWrapper(browsableSubFolders);
        List<Link> folderLinks = linkDao.getLinksByFolderIdAndLinkType(folderIdsForLinkQuery, LinkType.FOLDER)
                .stream().filter(accessFilter::hasBrowsePermissionForOwnable).collect(Collectors.toList());
        folderWrapper.setLinks(folderLinks);
        List<Long>   linkedFolderIds = folderLinks.stream().map(Link::getFolderId).collect(Collectors.toSet()).stream().toList();
        List<Folder> references      = folderDao.getFoldersById(linkedFolderIds, folderRequest.isIncludeSummary());
        folderWrapper.setReferences(references);
        response.setWrapper(folderWrapper);
    }

    private void updateFolder(CinnamonRequest request, CinnamonResponse response, UserAccount user, FolderDao folderDao) throws IOException {
        UpdateFolderRequest updateRequest = request.getMapper().readValue(request.getInputStream(), UpdateFolderRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());

        for (Folder updateFolder : updateRequest.getFolders()) {
            Boolean metadataChangedOverride = updateRequest.isUpdateMetadataChanged() ? updateFolder.isMetadataChanged() : null;
            folderService.updateFolder(updateFolder.getId(), updateFolder.getName(), updateFolder.getParentId(),
                    updateFolder.getAclId(), updateFolder.getOwnerId(), updateFolder.getTypeId(),
                    metadataChangedOverride, user);
        }
        response.responseIsGenericOkay();
    }

    private void deleteMeta(CinnamonRequest request, CinnamonResponse response, UserAccount user, FolderDao folderDao) throws IOException {
        DeleteMetaRequest metaRequest = (DeleteMetaRequest) request.getMapper().readValue(request.getInputStream(), DeleteMetaRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());

        FolderMetaDao folderMetaDao = new FolderMetaDao();
        List<Meta>    metas         = folderMetaDao.getObjectsById(metaRequest.getIds());
        if (metas.size() != metaRequest.getIds().size() && !metaRequest.isIgnoreNotFound()) {
            throw ErrorCode.METASET_NOT_FOUND.exception();
        }
        new MetaService<>().deleteMetas(folderMetaDao, metas, folderDao, user);
        response.setResponse(new DeleteResponse(true));
    }

    /**
     * Note: getMeta allows FolderMetaRequests without metaset name to return all metasets.
     * This usage is deprecated.
     * Current Cinnamon 3 clients expect the arbitrary metaset XML to be added to the DOM of the metaset wrapper.
     * This requires assembling a new DOM tree in memory for each request to getMeta, which is not something you
     * want to see with large metasets.
     */
    private void getMeta(CinnamonRequest request, CinnamonResponse response, FolderDao folderDao) throws IOException {
        MetaRequest metaRequest = request.getMapper().readValue(request.getInputStream(), MetaRequest.class)
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

    private void createMeta(CinnamonRequest request, CinnamonResponse response, UserAccount user, FolderDao folderDao) throws IOException {
        CreateMetaRequest metaRequest = request.getMapper().readValue(request.getInputStream(), CreateMetaRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());

        List<Meta> metas = new MetaService<>().createMeta(new FolderMetaDao(), metaRequest.getMetas(), folderDao, user, false);
        createMetaResponse(response, metas);
    }

    private void getFolderByPath(CinnamonRequest request, CinnamonResponse response, UserAccount user, FolderDao folderDao) throws IOException {
        FolderPathRequest pathRequest = request.getMapper().readValue(request.getInputStream(), FolderPathRequest.class);
        if (pathRequest.validated()) {
            List<Folder> rawFolders = folderDao.getFolderByPathWithAncestors(pathRequest.getPath(), pathRequest.isIncludeSummary());
            // TODO: if rawFolders is empty, and create flag is set, iteratively create path and children starting at root.
            List<Folder> folders    = new AuthorizationService().filterFoldersByBrowsePermission(rawFolders, user);
            if (folders.isEmpty()) {
                throw ErrorCode.OBJECT_NOT_FOUND.exception();
            }
            response.setWrapper(new FolderWrapper(folders));
        } else {
            throw ErrorCode.INVALID_REQUEST.exception();
        }
    }

    private void getFolderByRelativePath(CinnamonRequest request, CinnamonResponse response, UserAccount user, FolderDao folderDao) throws IOException {
        FolderByRelativePathRequest pathRequest = request.getMapper().readValue(request.getInputStream(), FolderByRelativePathRequest.class);
        if (pathRequest.validated()) {
            Folder parentFolder     = folderDao.getFolderById(pathRequest.getParentId()).orElseThrow(ErrorCode.OBJECT_NOT_FOUND.getException());
            String parentFolderPath = folderDao.getFolderPath(parentFolder.getId())
                    // root is added by folderDao below.
                    .replace("/root", "");
            String fullPath         = parentFolderPath + "/" + pathRequest.getRelativePath();

            List<Folder> rawFolders = folderDao.getFolderByPathWithAncestors(fullPath, pathRequest.isIncludeSummary());
            List<Folder> folders    = new AuthorizationService().filterFoldersByBrowsePermission(rawFolders, user);
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
    private void getFolder(CinnamonRequest request, CinnamonResponse response, UserAccount user, FolderDao folderDao) throws IOException {
        SingleFolderRequest folderRequest = request.getMapper().readValue(request.getInputStream(), SingleFolderRequest.class)
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
    private void getFolders(CinnamonRequest request, CinnamonResponse response, UserAccount user, FolderDao folderDao) throws IOException {
        FolderRequest folderRequest = request.getMapper().readValue(request.getInputStream(), FolderRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());

        List<Folder> rawFolders = folderDao.getFoldersById(folderRequest.getIds(), folderRequest.isIncludeSummary());
        List<Folder> folders    = new AuthorizationService().filterFoldersByBrowsePermission(rawFolders, user);
        if (folders.isEmpty()) {
            throw ErrorCode.FOLDER_NOT_FOUND.exception();
        }
        if(folderRequest.isAddFolderPath()) {
            List<Long> folderIds = folders.stream().map(Folder::getId).toList();
            Map<Long, String> folderPaths = folderDao.getFolderPaths(folderIds);
            for (Folder folder : folders) {
                folder.setFolderPath(folderPaths.get(folder.getId()));
            }
        }
        response.setWrapper(new FolderWrapper(folders));
    }

    private void setSummary(CinnamonRequest request, CinnamonResponse response, UserAccount user, FolderDao folderDao) throws IOException {
        // TODO: add SetSummaryRequest.validateRequest
        SetSummaryRequest summaryRequest = request.getMapper().readValue(request.getInputStream(), SetSummaryRequest.class);
        Optional<Folder>  folderOpt      = folderDao.getFolderById(summaryRequest.getId());
        if (folderOpt.isPresent()) {
            Folder folder = folderOpt.get();
            if (authorizationService.hasUserOrOwnerPermission(folder, DefaultPermission.SET_SUMMARY, user)) {
                folder.setSummary(summaryRequest.getSummary());
                folderDao.updateFolder(folder);
                response.responseIsGenericOkay();
                return;
            } else {
                throw ErrorCode.NO_SET_SUMMARY_PERMISSION.exception();
            }
        }
        ErrorCode.OBJECT_NOT_FOUND.throwUp();
    }

    private void getSummaries(CinnamonRequest request, CinnamonResponse response, UserAccount user, FolderDao folderDao) throws IOException {
        IdListRequest  idListRequest = request.getMapper().readValue(request.getInputStream(), IdListRequest.class);
        SummaryWrapper wrapper       = new SummaryWrapper();
        List<Folder>   folders       = folderDao.getFoldersById(idListRequest.getIds().stream().toList(), true);
        folders.forEach(folder -> {
            if (authorizationService.hasUserOrOwnerPermission(folder, DefaultPermission.BROWSE, user)) {
                wrapper.getSummaries().add(new Summary(folder.getId(), folder.getSummary()));
            } else {
                throw ErrorCode.NO_BROWSE_PERMISSION.exception();
            }
        });
        response.setWrapper(wrapper);
    }

    private List<Meta> createMetas(List<Meta> metas, Long id) {
        var dao = new FolderMetaDao();
        checkMetaUniqueness(metas);
        metas.forEach(meta -> meta.setObjectId(id));
        dao.create(metas);
        return dao.listByFolderId(id);
    }
}
