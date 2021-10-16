package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.DefaultPermission;
import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.FailedRequestException;
import com.dewarim.cinnamon.api.Constants;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.CinnamonResponse;
import com.dewarim.cinnamon.application.ResponseUtil;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.application.exception.BadArgumentException;
import com.dewarim.cinnamon.dao.AclDao;
import com.dewarim.cinnamon.dao.FolderDao;
import com.dewarim.cinnamon.dao.FolderMetaDao;
import com.dewarim.cinnamon.dao.FolderTypeDao;
import com.dewarim.cinnamon.dao.MetasetTypeDao;
import com.dewarim.cinnamon.dao.UserAccountDao;
import com.dewarim.cinnamon.model.Acl;
import com.dewarim.cinnamon.model.Folder;
import com.dewarim.cinnamon.model.FolderType;
import com.dewarim.cinnamon.model.Meta;
import com.dewarim.cinnamon.model.MetasetType;
import com.dewarim.cinnamon.model.UserAccount;
import com.dewarim.cinnamon.model.request.CreateMetaRequest;
import com.dewarim.cinnamon.model.request.DeleteMetaRequest;
import com.dewarim.cinnamon.model.request.IdListRequest;
import com.dewarim.cinnamon.model.request.MetaRequest;
import com.dewarim.cinnamon.model.request.SetSummaryRequest;
import com.dewarim.cinnamon.model.request.folder.CreateFolderRequest;
import com.dewarim.cinnamon.model.request.folder.FolderPathRequest;
import com.dewarim.cinnamon.model.request.folder.FolderRequest;
import com.dewarim.cinnamon.model.request.folder.SingleFolderRequest;
import com.dewarim.cinnamon.model.request.folder.UpdateFolderRequest;
import com.dewarim.cinnamon.model.response.FolderWrapper;
import com.dewarim.cinnamon.model.response.GenericResponse;
import com.dewarim.cinnamon.model.response.Summary;
import com.dewarim.cinnamon.model.response.SummaryWrapper;
import com.dewarim.cinnamon.security.authorization.AccessFilter;
import com.dewarim.cinnamon.security.authorization.AuthorizationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.dewarim.cinnamon.api.Constants.XML_MAPPER;
import static com.dewarim.cinnamon.application.ErrorResponseGenerator.generateErrorMessage;

@WebServlet(name = "Folder", urlPatterns = "/")
public class FolderServlet extends BaseServlet {

    private final ObjectMapper         xmlMapper            = XML_MAPPER;
    private final AuthorizationService authorizationService = new AuthorizationService();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        UserAccount      user             = ThreadLocalSqlSession.getCurrentUser();
        FolderDao        folderDao        = new FolderDao();
        CinnamonResponse cinnamonResponse = (CinnamonResponse) response;
        UrlMapping       mapping          = UrlMapping.getByPath(request.getRequestURI());

        switch (mapping) {
            case FOLDER__CREATE -> createFolder(request, response, user, folderDao);
            case FOLDER__CREATE_META -> createMeta(request, cinnamonResponse, user, folderDao);
            case FOLDER__DELETE_META -> deleteMeta(request, response, user, folderDao);
            case FOLDER__GET_FOLDER -> getFolder(request, response, user, folderDao);
            case FOLDER__GET_FOLDER_BY_PATH -> getFolderByPath(request, response, user, folderDao);
            case FOLDER__GET_FOLDERS -> getFolders(request, response, user, folderDao);
            case FOLDER__GET_META -> getMeta(request, cinnamonResponse, user, folderDao);
            case FOLDER__GET_SUBFOLDERS -> getSubFolders(request, response, user, folderDao);
            case FOLDER__SET_SUMMARY -> setSummary(request, response, user, folderDao);
            case FOLDER__GET_SUMMARIES -> getSummaries(request, response, user, folderDao);
            case FOLDER__UPDATE -> updateFolder(request, response, user, folderDao);
            default -> ErrorCode.RESOURCE_NOT_FOUND.throwUp();
        }
    }

    private void createFolder(HttpServletRequest request, HttpServletResponse response, UserAccount user, FolderDao folderDao) throws IOException {
        CreateFolderRequest createRequest = xmlMapper.readValue(request.getInputStream(), CreateFolderRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());
        Long parentId = createRequest.getParentId();
        Folder parentFolder = folderDao.getFolderById(parentId)
                .orElseThrow(ErrorCode.PARENT_FOLDER_NOT_FOUND.getException());

        AccessFilter accessFilter = AccessFilter.getInstance(user);
        if (!accessFilter.hasPermissionOnOwnable(parentFolder, DefaultPermission.CREATE_FOLDER, parentFolder)) {
            ErrorCode.NO_CREATE_PERMISSION.throwUp();
        }
        String name = createRequest.getName();
        folderDao.getFolderByParentAndName(parentFolder.getId(), name, false)
                .ifPresent(f -> ErrorCode.DUPLICATE_FOLDER_NAME_FORBIDDEN.throwUp());

        FolderTypeDao typeDao = new FolderTypeDao();
        Long          typeId  = createRequest.getTypeId();
        if (typeId == null) {
            typeId = typeDao.getFolderTypeByName(Constants.FOLDER_TYPE_DEFAULT)
                    .orElseThrow(ErrorCode.FOLDER_TYPE_NOT_FOUND.getException()).getId();
        } else {
            typeId = typeDao.getFolderTypeById(typeId)
                    .orElseThrow(ErrorCode.FOLDER_TYPE_NOT_FOUND.getException()).getId();
        }

        Long ownerId = createRequest.getOwnerId();
        if (ownerId == null) {
            ownerId = parentFolder.getOwnerId();
        } else {
            ownerId = new UserAccountDao().getUserAccountById(ownerId)
                    .orElseThrow(ErrorCode.USER_ACCOUNT_NOT_FOUND.getException()).getId();
        }

        Long aclId = createRequest.getAclId();
        if (aclId == null) {
            aclId = parentFolder.getAclId();
        } else {
            aclId = new AclDao().getAclById(aclId)
                    .orElseThrow(ErrorCode.ACL_NOT_FOUND.getException()).getId();
        }

        Folder folder      = new Folder(name, aclId, ownerId, parentId, typeId, createRequest.getSummary());
        Folder savedFolder = folderDao.saveFolder(folder);

        FolderWrapper wrapper = new FolderWrapper(Collections.singletonList(savedFolder));
        ResponseUtil.responseIsOkayAndXml(response);
        xmlMapper.writeValue(response.getWriter(), wrapper);

    }

    private void getSubFolders(HttpServletRequest request, HttpServletResponse response, UserAccount user, FolderDao folderDao) throws IOException {
        SingleFolderRequest folderRequest = xmlMapper.readValue(request.getInputStream(), SingleFolderRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());
        Folder             folder              = folderDao.getFolderById(folderRequest.getId()).orElseThrow(ErrorCode.FOLDER_NOT_FOUND.getException());
        List<Folder>       subFolders          = folderDao.getDirectSubFolders(folder.getId(), folderRequest.isIncludeSummary());
        final AccessFilter accessFilter        = AccessFilter.getInstance(user);
        List<Folder>       browsableSubFolders = subFolders.stream().filter(accessFilter::hasBrowsePermissionForOwnable).collect(Collectors.toList());
        FolderWrapper      wrapper             = new FolderWrapper(browsableSubFolders);
        ResponseUtil.responseIsOkayAndXml(response);
        xmlMapper.writeValue(response.getWriter(), wrapper);
    }

    private void updateFolder(HttpServletRequest request, HttpServletResponse response, UserAccount user, FolderDao folderDao) throws IOException {
        UpdateFolderRequest updateRequest = xmlMapper.readValue(request.getInputStream(), UpdateFolderRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());

        Long         folderId     = updateRequest.getId();
        Folder       folder       = folderDao.getFolderById(folderId, true).orElseThrow(ErrorCode.FOLDER_NOT_FOUND.getException());
        AccessFilter accessFilter = AccessFilter.getInstance(user);

        if (!accessFilter.hasPermissionOnOwnable(folder, DefaultPermission.EDIT_FOLDER, folder)) {
            ErrorCode.NO_EDIT_FOLDER_PERMISSION.throwUp();
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
            Folder parentFolder = folderDao.getFolderById(folder.getParentId())
                    .orElseThrow(ErrorCode.PARENT_FOLDER_NOT_FOUND.getException());
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

        ResponseUtil.responseIsGenericOkay(response);
    }

    private void deleteMeta(HttpServletRequest request, HttpServletResponse response, UserAccount user, FolderDao folderDao) throws IOException {
        DeleteMetaRequest metaRequest = xmlMapper.readValue(request.getInputStream(), DeleteMetaRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());
        Long   folderId = metaRequest.getId();
        Folder folder   = folderDao.getFolderById(folderId).orElseThrow(ErrorCode.FOLDER_NOT_FOUND.getException());
        throwUnlessCustomMetaIsWritable(folder, user);

        List<Meta>    metas;
        FolderMetaDao metaDao = new FolderMetaDao();
        if (metaRequest.getMetaId() != null) {
            metas = Collections.singletonList(metaDao.getFolderMetaById(metaRequest.getMetaId()));
        } else {
            metas = metaDao.getMetaByNamesAndFolderId(Collections.singletonList(metaRequest.getTypeName()), folderId);
        }
        if (metas.isEmpty()) {
            throw new FailedRequestException(ErrorCode.METASET_NOT_FOUND);
        }
        metas.forEach(meta -> {
            boolean deleteSuccess = metaDao.deleteById(meta.getId()) == 1;
            if (!deleteSuccess) {
                ErrorCode.DB_DELETE_FAILED.throwUp();
            }
        });
        ResponseUtil.responseIsGenericOkay(response);
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
        if (metaRequest.getTypeNames() != null) {
            metaList = new FolderMetaDao().getMetaByNamesAndFolderId(metaRequest.getTypeNames(), folderId);
        } else {
            metaList = new FolderMetaDao().listByFolderId(folderId);
        }

        createMetaResponse(metaRequest, response, metaList);
    }

    private void createMeta(HttpServletRequest request, CinnamonResponse response, UserAccount user, FolderDao folderDao) throws IOException {
        CreateMetaRequest metaRequest = xmlMapper.readValue(request.getInputStream(), CreateMetaRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());

        Long   folderId = metaRequest.getId();
        Folder folder   = folderDao.getFolderById(folderId).orElseThrow(ErrorCode.FOLDER_NOT_FOUND.getException());
        throwUnlessCustomMetaIsWritable(folder, user);
        MetasetType metaType;
        if (metaRequest.getTypeId() != null) {
            metaType = new MetasetTypeDao().getMetasetTypeById(metaRequest.getTypeId())
                    .orElseThrow(ErrorCode.METASET_TYPE_NOT_FOUND.getException());
        } else {
            metaType = new MetasetTypeDao().getMetasetTypeByName(metaRequest.getTypeName())
                    .orElseThrow(ErrorCode.METASET_TYPE_NOT_FOUND.getException());
        }

        // does meta already exist and is unique?
        FolderMetaDao metaDao = new FolderMetaDao();
        List<Meta>    metas   = metaDao.getMetaByNamesAndFolderId(Collections.singletonList(metaType.getName()), folderId);
        if (metaType.getUnique() && metas.size() > 0) {
            throw ErrorCode.METASET_IS_UNIQUE_AND_ALREADY_EXISTS.exception();
        }

        Meta meta = metaDao.createMeta(metaRequest, metaType);
        createMetaResponse(new MetaRequest(), response, Collections.singletonList(meta));
    }

    private void getFolderByPath(HttpServletRequest request, HttpServletResponse response, UserAccount user, FolderDao folderDao) throws IOException {
        FolderPathRequest pathRequest = xmlMapper.readValue(request.getInputStream(), FolderPathRequest.class);
        if (pathRequest.validated()) {

            List<Folder> rawFolders;
            try {
                rawFolders = folderDao.getFolderByPathWithAncestors(pathRequest.getPath(), pathRequest.isIncludeSummary());
            } catch (BadArgumentException e) {
                generateErrorMessage(response, e.getErrorCode());
                return;
            }
            List<Folder> folders = new AuthorizationService().filterFoldersByBrowsePermission(rawFolders, user);
            if (folders.isEmpty()) {
                generateErrorMessage(response, ErrorCode.OBJECT_NOT_FOUND);
                return;
            }

            ResponseUtil.responseIsOkayAndXml(response);
            FolderWrapper folderWrapper = new FolderWrapper();
            folderWrapper.setFolders(folders);
            xmlMapper.writeValue(response.getWriter(), folderWrapper);
        } else {
            generateErrorMessage(response, ErrorCode.INVALID_REQUEST);
        }
    }


    /**
     * Retrieve a single folder, including ancestors.
     */
    private void getFolder(HttpServletRequest request, HttpServletResponse response, UserAccount user, FolderDao folderDao) throws IOException {
        SingleFolderRequest folderRequest = xmlMapper.readValue(request.getInputStream(), SingleFolderRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());

        List<Folder> rawFolders = folderDao.getFolderByIdWithAncestors(folderRequest.getId(), folderRequest.isIncludeSummary());
        List<Folder> folders    = new AuthorizationService().filterFoldersByBrowsePermission(rawFolders, user);
        if (folders.isEmpty()) {
            generateErrorMessage(response, ErrorCode.OBJECT_NOT_FOUND);
            return;
        }

        ResponseUtil.responseIsOkayAndXml(response);
        FolderWrapper folderWrapper = new FolderWrapper();
        folderWrapper.setFolders(folders);
        xmlMapper.writeValue(response.getWriter(), folderWrapper);
    }

    /**
     * Retrieve a list of folders, without including their ancestors.
     */
    private void getFolders(HttpServletRequest request, HttpServletResponse response, UserAccount user, FolderDao folderDao) throws IOException {
        FolderRequest folderRequest = xmlMapper.readValue(request.getInputStream(), FolderRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());

        List<Folder> rawFolders = folderDao.getFoldersById(folderRequest.getIds(), folderRequest.isIncludeSummary());
        List<Folder> folders    = new AuthorizationService().filterFoldersByBrowsePermission(rawFolders, user);
        if (folders.isEmpty()) {
            generateErrorMessage(response, ErrorCode.OBJECT_NOT_FOUND);
            return;
        }

        ResponseUtil.responseIsOkayAndXml(response);
        FolderWrapper folderWrapper = new FolderWrapper();
        folderWrapper.setFolders(folders);
        xmlMapper.writeValue(response.getWriter(), folderWrapper);
    }

    private void setSummary(HttpServletRequest request, HttpServletResponse response, UserAccount user, FolderDao folderDao) throws IOException {
        SetSummaryRequest summaryRequest = xmlMapper.readValue(request.getInputStream(), SetSummaryRequest.class);
        Optional<Folder>  folderOpt      = folderDao.getFolderById(summaryRequest.getId());
        if (folderOpt.isPresent()) {
            Folder folder = folderOpt.get();
            if (authorizationService.hasUserOrOwnerPermission(folder, DefaultPermission.WRITE_OBJECT_SYS_METADATA, user)) {
                folder.setSummary(summaryRequest.getSummary());
                folderDao.updateFolder(folder);
                ResponseUtil.responseIsOkayAndXml(response);
                xmlMapper.writeValue(response.getWriter(), new GenericResponse(true));
                return;
            } else {
                generateErrorMessage(response, ErrorCode.NO_WRITE_SYS_METADATA_PERMISSION);
                return;
            }
        }
        generateErrorMessage(response, ErrorCode.OBJECT_NOT_FOUND);
    }

    private void getSummaries(HttpServletRequest request, HttpServletResponse response, UserAccount user, FolderDao folderDao) throws IOException {
        IdListRequest  idListRequest = xmlMapper.readValue(request.getInputStream(), IdListRequest.class);
        SummaryWrapper wrapper       = new SummaryWrapper();
        List<Folder>   folders       = folderDao.getFoldersById(idListRequest.getIdList(), true);
        folders.forEach(folder -> {
            if (authorizationService.hasUserOrOwnerPermission(folder, DefaultPermission.READ_OBJECT_SYS_METADATA, user)) {
                wrapper.getSummaries().add(new Summary(folder.getId(), folder.getSummary()));
            }
        });
        ResponseUtil.responseIsOkayAndXml(response);
        xmlMapper.writeValue(response.getWriter(), wrapper);
    }


}
