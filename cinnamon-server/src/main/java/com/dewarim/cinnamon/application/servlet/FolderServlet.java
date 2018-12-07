package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.DefaultPermission;
import com.dewarim.cinnamon.application.ErrorCode;
import com.dewarim.cinnamon.application.ErrorResponseGenerator;
import com.dewarim.cinnamon.application.ResponseUtil;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.application.exception.BadArgumentException;
import com.dewarim.cinnamon.application.exception.FailedRequestException;
import com.dewarim.cinnamon.dao.*;
import com.dewarim.cinnamon.model.*;
import com.dewarim.cinnamon.model.request.*;
import com.dewarim.cinnamon.model.response.FolderWrapper;
import com.dewarim.cinnamon.model.response.GenericResponse;
import com.dewarim.cinnamon.model.response.SummaryWrapper;
import com.dewarim.cinnamon.security.authorization.AccessFilter;
import com.dewarim.cinnamon.security.authorization.AuthorizationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.dewarim.cinnamon.application.ErrorResponseGenerator.generateErrorMessage;
import static javax.servlet.http.HttpServletResponse.*;

@WebServlet(name = "Folder", urlPatterns = "/")
public class FolderServlet extends BaseServlet {

    private ObjectMapper         xmlMapper            = new XmlMapper();
    private AuthorizationService authorizationService = new AuthorizationService();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            pathInfo = "";
        }
        UserAccount user      = ThreadLocalSqlSession.getCurrentUser();
        FolderDao   folderDao = new FolderDao();
        try {
            switch (pathInfo) {
                case "/createMeta":
                    createMeta(request, response, user, folderDao);
                    break;
                case "/deleteMeta":
                    deleteMeta(request, response, user, folderDao);
                    break;
                case "/getFolder":
                    getFolder(request, response, user, folderDao);
                    break;
                case "/getFolderByPath":
                    getFolderByPath(request, response, user, folderDao);
                    break;
                case "/getFolders":
                    getFolders(request, response, user, folderDao);
                    break;
                case "/getMeta":
                    getMeta(request, response, user, folderDao);
                    break;
                case "/setSummary":
                    setSummary(request, response, user, folderDao);
                    break;
                case "/getSummaries":
                    getSummaries(request, response, user, folderDao);
                    break;
                case "/updateFolder":
                    updateFolder(request, response, user, folderDao);
                    break;
                default:
                    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            }
        } catch (FailedRequestException e) {
            ErrorCode errorCode = e.getErrorCode();
            ErrorResponseGenerator.generateErrorMessage(response, errorCode.getHttpResponseCode(), errorCode, e.getMessage());
        }
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
        Long parentId =updateRequest.getParentId();
        if (parentId != null) {
            if(parentId.equals(folderId)){
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
            folder.setParentId(parentFolder.getParentId());
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
            Acl acl = new AclDao().getAclByIdOpt(aclId)
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
                generateErrorMessage(response, SC_INTERNAL_SERVER_ERROR, ErrorCode.DB_DELETE_FAILED);
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
    private void getMeta(HttpServletRequest request, HttpServletResponse response, UserAccount user, FolderDao folderDao) throws IOException {
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

        createMetaResponse(metaRequest, response, metaList, xmlMapper);
    }

    private void createMeta(HttpServletRequest request, HttpServletResponse response, UserAccount user, FolderDao folderDao) throws IOException {
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
            throw new FailedRequestException(ErrorCode.METASET_IS_UNIQUE_AND_ALREADY_EXISTS);
        }

        Meta meta = metaDao.createMeta(metaRequest, metaType);
        createMetaResponse(new MetaRequest(), response, Collections.singletonList(meta), xmlMapper);
    }

    private void getFolderByPath(HttpServletRequest request, HttpServletResponse response, UserAccount user, FolderDao folderDao) throws IOException {
        FolderPathRequest pathRequest = xmlMapper.readValue(request.getInputStream(), FolderPathRequest.class);
        if (pathRequest.validated()) {

            List<Folder> rawFolders;
            try {
                rawFolders = folderDao.getFolderByPathWithAncestors(pathRequest.getPath(), pathRequest.isIncludeSummary());
            } catch (BadArgumentException e) {
                generateErrorMessage(response, SC_BAD_REQUEST, e.getErrorCode());
                return;
            }
            List<Folder> folders = new AuthorizationService().filterFoldersByBrowsePermission(rawFolders, user);
            if (folders.isEmpty()) {
                generateErrorMessage(response, SC_NOT_FOUND, ErrorCode.OBJECT_NOT_FOUND);
                return;
            }

            ResponseUtil.responseIsOkayAndXml(response);
            FolderWrapper folderWrapper = new FolderWrapper();
            folderWrapper.setFolders(folders);
            xmlMapper.writeValue(response.getWriter(), folderWrapper);
        } else {
            generateErrorMessage(response, SC_BAD_REQUEST, ErrorCode.INVALID_REQUEST);
        }
    }


    /**
     * Retrieve a single folder, including ancestors.
     */
    private void getFolder(HttpServletRequest request, HttpServletResponse response, UserAccount user, FolderDao folderDao) throws IOException {
        SingleFolderRequest folderRequest = xmlMapper.readValue(request.getInputStream(), SingleFolderRequest.class);
        if (folderRequest.validated()) {
            List<Folder> rawFolders = folderDao.getFolderByIdWithAncestors(folderRequest.getId(), folderRequest.isIncludeSummary());
            List<Folder> folders    = new AuthorizationService().filterFoldersByBrowsePermission(rawFolders, user);
            if (folders.isEmpty()) {
                generateErrorMessage(response, SC_NOT_FOUND, ErrorCode.OBJECT_NOT_FOUND);
                return;
            }

            ResponseUtil.responseIsOkayAndXml(response);
            FolderWrapper folderWrapper = new FolderWrapper();
            folderWrapper.setFolders(folders);
            xmlMapper.writeValue(response.getWriter(), folderWrapper);
        } else {
            generateErrorMessage(response, SC_BAD_REQUEST, ErrorCode.INVALID_REQUEST);
        }
    }

    /**
     * Retrieve a list of folders, without including their ancestors.
     */
    private void getFolders(HttpServletRequest request, HttpServletResponse response, UserAccount user, FolderDao folderDao) throws IOException {
        FolderRequest folderRequest = xmlMapper.readValue(request.getInputStream(), FolderRequest.class);
        if (folderRequest.validated()) {

            List<Folder> rawFolders = folderDao.getFoldersById(folderRequest.getIds(), folderRequest.isIncludeSummary());
            List<Folder> folders    = new AuthorizationService().filterFoldersByBrowsePermission(rawFolders, user);
            if (folders.isEmpty()) {
                generateErrorMessage(response, SC_NOT_FOUND, ErrorCode.OBJECT_NOT_FOUND);
                return;
            }

            ResponseUtil.responseIsOkayAndXml(response);
            FolderWrapper folderWrapper = new FolderWrapper();
            folderWrapper.setFolders(folders);
            xmlMapper.writeValue(response.getWriter(), folderWrapper);
        } else {
            generateErrorMessage(response, SC_BAD_REQUEST, ErrorCode.INVALID_REQUEST);
        }
    }

    private void setSummary(HttpServletRequest request, HttpServletResponse response, UserAccount user, FolderDao folderDao) throws IOException {
        SetSummaryRequest summaryRequest = xmlMapper.readValue(request.getInputStream(), SetSummaryRequest.class);
        Optional<Folder>  folderOpt      = folderDao.getFolderById(summaryRequest.getId());
        if (folderOpt.isPresent()) {
            Folder folder = folderOpt.get();
            if (authorizationService.hasUserOrOwnerPermission(folder, DefaultPermission.WRITE_OBJECT_SYS_METADATA.getName(), user)) {
                folder.setSummary(summaryRequest.getSummary());
                folderDao.updateFolder(folder);
                ResponseUtil.responseIsOkayAndXml(response);
                xmlMapper.writeValue(response.getWriter(), new GenericResponse(true));
                return;
            } else {
                generateErrorMessage(response, HttpServletResponse.SC_FORBIDDEN, ErrorCode.NO_WRITE_SYS_METADATA_PERMISSION);
                return;
            }
        }
        generateErrorMessage(response, HttpServletResponse.SC_NOT_FOUND, ErrorCode.OBJECT_NOT_FOUND);
    }

    private void getSummaries(HttpServletRequest request, HttpServletResponse response, UserAccount user, FolderDao folderDao) throws IOException {
        IdListRequest  idListRequest = xmlMapper.readValue(request.getInputStream(), IdListRequest.class);
        SummaryWrapper wrapper       = new SummaryWrapper();
        List<Folder>   folders       = folderDao.getFoldersById(idListRequest.getIdList(), true);
        folders.forEach(folder -> {
            if (authorizationService.hasUserOrOwnerPermission(folder, DefaultPermission.READ_OBJECT_SYS_METADATA.getName(), user)) {
                wrapper.getSummaries().add(folder.getSummary());
            }
        });
        ResponseUtil.responseIsOkayAndXml(response);
        xmlMapper.writeValue(response.getWriter(), wrapper);
    }


}
