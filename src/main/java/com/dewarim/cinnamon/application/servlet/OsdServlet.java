package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.DefaultPermission;
import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.FailedRequestException;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.api.content.ContentMetadata;
import com.dewarim.cinnamon.api.content.ContentProvider;
import com.dewarim.cinnamon.api.lifecycle.State;
import com.dewarim.cinnamon.api.lifecycle.StateChangeResult;
import com.dewarim.cinnamon.application.CinnamonRequest;
import com.dewarim.cinnamon.application.CinnamonResponse;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.application.service.DeleteOsdService;
import com.dewarim.cinnamon.application.service.MetaService;
import com.dewarim.cinnamon.application.service.TikaService;
import com.dewarim.cinnamon.dao.*;
import com.dewarim.cinnamon.model.*;
import com.dewarim.cinnamon.model.links.Link;
import com.dewarim.cinnamon.model.links.LinkType;
import com.dewarim.cinnamon.model.relations.Relation;
import com.dewarim.cinnamon.model.relations.RelationType;
import com.dewarim.cinnamon.model.request.IdListRequest;
import com.dewarim.cinnamon.model.request.IdRequest;
import com.dewarim.cinnamon.model.request.meta.*;
import com.dewarim.cinnamon.model.request.osd.*;
import com.dewarim.cinnamon.model.response.*;
import com.dewarim.cinnamon.provider.ContentProviderService;
import com.dewarim.cinnamon.provider.StateProviderService;
import com.dewarim.cinnamon.security.authorization.AccessFilter;
import com.dewarim.cinnamon.security.authorization.AuthorizationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.dewarim.cinnamon.ErrorCode.*;
import static com.dewarim.cinnamon.api.Constants.*;
import static jakarta.servlet.http.HttpServletResponse.SC_OK;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.apache.hc.core5.http.HttpHeaders.CONTENT_DISPOSITION;

@MultipartConfig
@WebServlet(name = "Osd", urlPatterns = "/")
public class OsdServlet extends BaseServlet implements CruddyServlet<ObjectSystemData> {

    private final        ObjectMapper           xmlMapper            = XML_MAPPER;
    private final        AuthorizationService   authorizationService = new AuthorizationService();
    private final        DeleteOsdService       deleteOsdService     = new DeleteOsdService();
    private static final Logger                 log                  = LogManager.getLogger(OsdServlet.class);
    private              TikaService            tikaService;
    private              ContentProviderService contentProviderService;

    public OsdServlet() {
        super();
        ThreadLocalSqlSession.refreshSession();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        UserAccount user   = ThreadLocalSqlSession.getCurrentUser();
        OsdDao      osdDao = new OsdDao();

        CinnamonRequest  cinnamonRequest  = (CinnamonRequest) request;
        CinnamonResponse cinnamonResponse = (CinnamonResponse) response;
        UrlMapping       mapping          = UrlMapping.getByPath(request.getRequestURI());
        switch (mapping) {
            case OSD__COPY -> copyOsd(cinnamonRequest, cinnamonResponse, user, osdDao);
            case OSD__COPY_TO_EXISTING -> copyToExistingOsd(cinnamonRequest, cinnamonResponse, user, osdDao);
            case OSD__CREATE_OSD -> createOsd(cinnamonRequest, cinnamonResponse, user, osdDao);
            case OSD__CREATE_META -> createMeta(cinnamonRequest, cinnamonResponse, user, osdDao);
            case OSD__DELETE -> deleteOsds(cinnamonRequest, cinnamonResponse, user, osdDao);
            case OSD__DELETE_META -> deleteMeta(cinnamonRequest, cinnamonResponse, user, osdDao);
            case OSD__DELETE_ALL_METAS -> deleteAllMetas(cinnamonRequest, cinnamonResponse, user, osdDao);
            case OSD__GET_CONTENT -> getContent(cinnamonRequest, cinnamonResponse, user, osdDao);
            case OSD__GET_META -> getMeta(cinnamonRequest, cinnamonResponse, user, osdDao);
            case OSD__GET_OBJECTS_BY_FOLDER_ID -> getObjectsByFolderId(cinnamonRequest, cinnamonResponse, user, osdDao);
            case OSD__GET_OBJECTS_BY_ID -> getObjectsById(cinnamonRequest, cinnamonResponse, user, osdDao);
            case OSD__GET_RELATIONS -> getRelations(cinnamonRequest, cinnamonResponse, user, osdDao);
            case OSD__GET_SUMMARIES -> getSummaries(cinnamonRequest, cinnamonResponse, user, osdDao);
            case OSD__LOCK -> lock(cinnamonRequest, cinnamonResponse, user, osdDao);
            case OSD__SET_CONTENT -> setContent(cinnamonRequest, cinnamonResponse, user, osdDao);
            case OSD__SET_SUMMARY -> setSummary(cinnamonRequest, cinnamonResponse, user, osdDao);
            case OSD__UNLOCK -> unlock(cinnamonRequest, cinnamonResponse, user, osdDao);
            case OSD__UPDATE -> update(cinnamonRequest, cinnamonResponse, user, osdDao);
            case OSD__UPDATE_META_CONTENT -> updateMetaContent(cinnamonRequest, cinnamonResponse, user, osdDao);
            case OSD__VERSION -> newVersion(cinnamonRequest, cinnamonResponse, user, osdDao);
            default -> ErrorCode.RESOURCE_NOT_FOUND.throwUp();
        }
    }

    private void copyToExistingOsd(CinnamonRequest request, CinnamonResponse cinnamonResponse, UserAccount user, OsdDao osdDao) throws IOException {
        CopyToExistingOsdRequest copyToExistingOsdRequest = xmlMapper.readValue(request.getInputStream(), CopyToExistingOsdRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());

        // get OSDs
        List<CopyTask>              copyTasks  = copyToExistingOsdRequest.getCopyTasks();
        List<Long>                  sourceIds  = copyTasks.stream().map(CopyTask::getSourceOsdId).toList();
        List<Long>                  targetIds  = copyTasks.stream().map(CopyTask::getTargetOsdId).toList();
        Map<Long, ObjectSystemData> sourceOsds = osdDao.getObjectsById(sourceIds, false).stream().collect(Collectors.toMap(ObjectSystemData::getId, Function.identity()));
        Map<Long, ObjectSystemData> targetOsds = osdDao.getObjectsById(targetIds, false).stream().collect(Collectors.toMap(ObjectSystemData::getId, Function.identity()));

        // check: set of source & target ids not overlapping
        preventOverlapOfSourceAndTargetOsds(sourceIds, targetIds);
        // check all permissions before performing any further actions:
        var errors = checkPermissionsOnCopyToExistingOsd(sourceOsds, targetOsds, copyTasks, user);
        if (errors.size() > 0) {
            throw new FailedRequestException(COPY_TO_EXISTING_FAILED, errors);
        }

        List<Deletion> deletions  = new ArrayList<>();
        var            metasetDao = new OsdMetaDao();
        for (CopyTask copyTask : copyTasks) {
            long             targetId = copyTask.getTargetOsdId();
            long             sourceId = copyTask.getSourceOsdId();
            ObjectSystemData target   = targetOsds.get(targetId);
            ObjectSystemData source   = sourceOsds.get(sourceId);
            if (copyTask.isCopyContent()) {
                String contentPath = target.getContentPath();
                if (contentPath != null && !contentPath.isEmpty()) {
                    deletions.add(new Deletion(targetId, contentPath, false));
                }
                copyContent(user, osdDao, target, source, source.getFormatId());
            }
            if (copyTask.getMetasetTypeIds() != null && !copyTasks.isEmpty()) {
                // delete _ALL_ metasets on target: (cinnamon3 behavior)
                metasetDao.delete(metasetDao.listByOsd(targetId).stream().map(Meta::getId).toList());
                List<Meta> metas = metasetDao.listByOsd(sourceId);
                var metaCopies = metas.stream()
                        .filter(meta -> copyTask.getMetasetTypeIds().contains(meta.getTypeId()))
                        .map(meta -> new Meta(targetId, meta.getTypeId(), meta.getContent()))
                        .collect(Collectors.toList());
                metasetDao.create(metaCopies);
                if (user.isChangeTracking()) {
                    target.setMetadataChanged(true);
                }
            }
        }
        if (!deletions.isEmpty()) {
            new DeletionDao().create(deletions);
        }
        cinnamonResponse.setResponse(new GenericResponse(true));
    }

    private void preventOverlapOfSourceAndTargetOsds(List<Long> sourceIds, List<Long> targetIds) {
        if (new HashSet<>(sourceIds).removeAll(new HashSet<>(targetIds))) {
            throw COPY_TO_EXISTING_OVERLAP.exception();
        }
    }

    private List<CinnamonError> checkPermissionsOnCopyToExistingOsd(Map<Long, ObjectSystemData> sourceOsds, Map<Long, ObjectSystemData> targetOsds, List<CopyTask> copyTasks, UserAccount user) {
        AccessFilter        accessFilter = AccessFilter.getInstance(user);
        List<CinnamonError> errors       = new ArrayList<>();
        for (CopyTask task : copyTasks) {
            ObjectSystemData source   = sourceOsds.get(task.getSourceOsdId());
            ObjectSystemData target   = targetOsds.get(task.getTargetOsdId());
            long             sourceId = source.getId();
            long             targetId = target.getId();
            // check basic browse permission - if user is not allowed to read an OSD, they have no business with it.
            if (!accessFilter.hasPermissionOnOwnable(source, DefaultPermission.BROWSE, source)) {
                errors.add(new CinnamonError(NO_BROWSE_PERMISSION.getCode(), sourceId));
            }
            if (!accessFilter.hasPermissionOnOwnable(target, DefaultPermission.BROWSE, target)) {
                errors.add(new CinnamonError(NO_BROWSE_PERMISSION.getCode(), targetId));
            }
            if (!task.getMetasetTypeIds().isEmpty()) {
                // check permissions to read and write metasets:
                if (!accessFilter.hasPermissionOnOwnable(source, DefaultPermission.READ_OBJECT_CUSTOM_METADATA, source)) {
                    errors.add(new CinnamonError(NO_READ_CUSTOM_METADATA_PERMISSION.getCode(), sourceId));
                }
                if (!accessFilter.hasPermissionOnOwnable(target, DefaultPermission.WRITE_OBJECT_CUSTOM_METADATA, target)) {
                    errors.add(new CinnamonError(NO_WRITE_CUSTOM_METADATA_PERMISSION.getCode(), targetId));
                }
            }
            if (task.isCopyContent()) {
                // check permissions to read and write content:
                if (!accessFilter.hasPermissionOnOwnable(source, DefaultPermission.READ_OBJECT_CONTENT, source)) {
                    errors.add(new CinnamonError(NO_READ_PERMISSION.getCode(), sourceId));
                }
                if (!accessFilter.hasPermissionOnOwnable(target, DefaultPermission.WRITE_OBJECT_CONTENT, target)) {
                    errors.add(new CinnamonError(NO_WRITE_PERMISSION.getCode(), targetId));
                }
                if (!target.lockedByUser(user)) {
                    errors.add(new CinnamonError(OBJECT_MUST_BE_LOCKED_BY_USER.getCode(), targetId));
                }
            }
        }
        return errors;
    }

    private void copyContent(UserAccount user, OsdDao osdDao, ObjectSystemData target, ObjectSystemData source, Long formatId) throws IOException {
        ContentProvider contentProvider = contentProviderService.getContentProvider(source.getContentProvider());
        try (InputStream contentStream = contentProvider.getContentStream(source)) {
            ContentMetadata metadata = contentProvider.writeContentStream(target, contentStream);
            target.setContentHash(metadata.getContentHash());
            target.setContentPath(metadata.getContentPath());
            target.setContentSize(metadata.getContentSize());
            target.setContentProvider(contentProvider.getName());
            target.setFormatId(formatId);
            Format format = new FormatDao().getObjectById(source.getFormatId()).orElseThrow();
            tikaService.convertContentToTikaMetaset(target, contentProvider.getContentStream(metadata), format);
            if (user.isChangeTracking()) {
                target.setContentChanged(true);
            }
            osdDao.updateOsd(target, false);
        }
    }

    private void updateMetaContent(CinnamonRequest request, CinnamonResponse cinnamonResponse, UserAccount user, OsdDao osdDao) throws IOException {
        UpdateMetaRequest metaRequest = (UpdateMetaRequest) getMapper().readValue(request.getInputStream(), UpdateMetaRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());
        OsdMetaDao osdMetaDao = new OsdMetaDao();
        new MetaService<>().updateMeta(osdMetaDao, metaRequest.getMetas(), osdDao, user);
        cinnamonResponse.setResponse(new GenericResponse(true));
    }

    private void deleteAllMetas(CinnamonRequest request, CinnamonResponse cinnamonResponse, UserAccount user, OsdDao osdDao) throws IOException {
        DeleteAllMetasRequest metaRequest = (DeleteAllMetasRequest) getMapper().readValue(request.getInputStream(), DeleteAllMetasRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());

        OsdMetaDao metaDao = new OsdMetaDao();
        List<Meta> metas   = metaDao.listMetaByObjectIds(metaRequest.getIds().stream().toList());
        new MetaService<>().deleteMetas(metaDao, metas, osdDao, user);
        cinnamonResponse.setWrapper(new DeleteResponse(true));
    }

    private void getRelations(CinnamonRequest request, CinnamonResponse cinnamonResponse, UserAccount user, OsdDao osdDao) throws IOException {
        GetRelationsRequest relationRequest = xmlMapper.readValue(request.getInputStream(), GetRelationsRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());

        var                    accessFilter = AccessFilter.getInstance(user);
        List<ObjectSystemData> osds         = osdDao.getObjectsById(relationRequest.getIds(), false);
        boolean hasReadSysMetaPermission = osds.stream().allMatch(osd ->
                accessFilter.hasPermissionOnOwnable(osd, DefaultPermission.BROWSE, osd));
        if (!hasReadSysMetaPermission) {
            ErrorCode.NO_BROWSE_PERMISSION.throwUp();
            return;
        }
        List<Long>      ids             = osds.stream().map(ObjectSystemData::getId).collect(Collectors.toList());
        List<Relation>  relations       = new RelationDao().getRelationsOrMode(ids, ids, null, relationRequest.isIncludeMetadata());
        RelationWrapper relationWrapper = new RelationWrapper(relations);
        cinnamonResponse.setWrapper(relationWrapper);
    }

    private void copyOsd(CinnamonRequest request, CinnamonResponse cinnamonResponse, UserAccount user, OsdDao osdDao) throws IOException {
        CopyOsdRequest copyOsdRequest = xmlMapper.readValue(request.getInputStream(), CopyOsdRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());
        AccessFilter accessFilter = AccessFilter.getInstance(user);

        // get OSDs
        List<ObjectSystemData> sourceOsds = osdDao.getObjectsById(copyOsdRequest.getSourceIds(), true);
        boolean hasContentPermission = sourceOsds.stream().allMatch(osd ->
                accessFilter.hasPermissionOnOwnable(osd, DefaultPermission.READ_OBJECT_CONTENT, osd));
        if (!hasContentPermission) {
            ErrorCode.NO_READ_PERMISSION.throwUp();
            return;
        }
        boolean hasCustomMetadataPermission = sourceOsds.stream().allMatch(osd ->
                accessFilter.hasPermissionOnOwnable(osd, DefaultPermission.READ_OBJECT_CUSTOM_METADATA, osd));
        if (!hasCustomMetadataPermission) {
            ErrorCode.NO_READ_CUSTOM_METADATA_PERMISSION.throwUp();
            return;
        }
        boolean hasReadSysMetaPermission = sourceOsds.stream().allMatch(osd ->
                accessFilter.hasPermissionOnOwnable(osd, DefaultPermission.BROWSE, osd));
        if (!hasReadSysMetaPermission) {
            ErrorCode.NO_BROWSE_PERMISSION.throwUp();
            return;
        }

        // get target folder
        FolderDao folderDao = new FolderDao();
        Folder targetFolder = folderDao.getFolderById(copyOsdRequest.getTargetFolderId())
                .orElseThrow(FOLDER_NOT_FOUND.getException());
        if (!accessFilter.hasPermissionOnOwnable(targetFolder, DefaultPermission.CREATE_OBJECT, targetFolder)) {
            ErrorCode.NO_CREATE_PERMISSION.throwUp();
            return;
        }

        // create copy
        var                    relationDao       = new RelationDao();
        var                    lifecycleStateDao = new LifecycleStateDao();
        List<ObjectSystemData> copies            = new ArrayList<>();
        for (ObjectSystemData osd : sourceOsds) {
            long id     = osd.getId();
            long userId = user.getId();
            var  copy   = new ObjectSystemData();
            copy.setAclId(targetFolder.getAclId());
            copy.setParentId(targetFolder.getId());
            copy.setName("Copy_" + osd.getName());
            copy.setPredecessorId(null);
            copy.setOwnerId(userId);
            copy.setCmnVersion("1");
            copy.setLatestBranch(true);
            copy.setLatestHead(true);
            copy.setRootId(null);
            copy.setModifierId(userId);
            copy.setModified(new Date());
            copy.setCreatorId(userId);
            copy.setCreated(new Date());
            copy.setLockerId(null);
            copy.setLatestHead(true);
            copy.setLatestBranch(true);
            copy.setTypeId(osd.getTypeId());
            copy.setLanguageId(osd.getLanguageId());
            copy.setSummary(osd.getSummary());
            osdDao.saveOsd(copy);

            // copy relations
            List<Relation> relations = relationDao.getRelationsToCopy(osd.getId());
            Map<Long, RelationType> relationTypes = new RelationTypeDao()
                    .getRelationTypeMap(relations.stream().map(Relation::getTypeId).collect(Collectors.toSet()));
            List<Relation> relationCopies = new ArrayList<>();
            for (Relation relation : relations) {
                RelationType relationType = relationTypes.get(relation.getTypeId());
                if (relationType.isCloneOnLeftCopy() && relation.getLeftId().equals(id)) {
                    Relation rel = new Relation(copy.getId(), relation.getRightId(), relation.getTypeId(), relation.getMetadata());
                    relationCopies.add(rel);
                }
                if (relationType.isCloneOnRightCopy() && relation.getRightId().equals(id)) {
                    Relation rel = new Relation(relation.getLeftId(), copy.getId(), relation.getTypeId(), relation.getMetadata());
                    relationCopies.add(rel);
                }
            }
            relationDao.create(relationCopies);

            // check lifecycle_state_on_copy
            if (osd.getLifecycleStateId() != null) {
                LifecycleState state = lifecycleStateDao.getLifecycleStateById(osd.getLifecycleStateId())
                        .orElseThrow(ErrorCode.LIFECYCLE_STATE_NOT_FOUND.getException());
                copy.setLifecycleStateId(state.getLifecycleStateForCopyId());
            }

            // copy metasets
            if (copyOsdRequest.getMetasetTypeIds().size() > 0) {
                var        metasetDao = new OsdMetaDao();
                List<Meta> metas      = metasetDao.listByOsd(osd.getId());
                var metaCopies = metas.stream()
                        .filter(meta -> copyOsdRequest.getMetasetTypeIds().contains(meta.getTypeId()))
                        .map(meta -> new Meta(copy.getId(), meta.getTypeId(), meta.getContent()))
                        .collect(Collectors.toList());
                new MetaService<>().createMeta(metasetDao, metaCopies, osdDao, user);
            }

            // copy content
            if (osd.getContentHash() != null) {
                copyContent(user, osdDao, copy, osd, osd.getFormatId());
            }
            copies.add(copy);
        }

        cinnamonResponse.setWrapper(new OsdWrapper(copies));
    }


    private void deleteOsds(CinnamonRequest request, CinnamonResponse cinnamonResponse, UserAccount user, OsdDao osdDao) throws IOException {
        DeleteOsdRequest deleteRequest = xmlMapper.readValue(request.getInputStream(), DeleteOsdRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());
        List<ObjectSystemData> osds = osdDao.getObjectsById(deleteRequest.getIds(), false);
        // reverse sort by id, so we try to delete descendants first.
        osds.sort(Comparator.comparingLong(ObjectSystemData::getId).reversed());
        boolean deleteDescendants = deleteRequest.isDeleteDescendants() || deleteRequest.isDeleteAllVersions();
        boolean deleteAllVersions = deleteRequest.isDeleteAllVersions();

        deleteOsdService.verifyAndDelete(osds, deleteDescendants, deleteAllVersions, user);

        // TODO: deleteContent? -> cleanup process? #199
        var deleteResponse = new DeleteResponse(true);
        cinnamonResponse.setWrapper(deleteResponse);
    }


    private void createOsd(CinnamonRequest request, CinnamonResponse response, UserAccount user, OsdDao osdDao) throws
            IOException, ServletException {
        verifyIsMultipart(request);
        Part contentRequest = request.getPart(CINNAMON_REQUEST_PART);
        if (contentRequest == null) {
            throw ErrorCode.MISSING_REQUEST_PAYLOAD.exception();
        }
        CreateOsdRequest createRequest = xmlMapper.readValue(contentRequest.getInputStream(), CreateOsdRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());

        // check parent folder exists
        Folder parentFolder = new FolderDao().getFolderById(createRequest.getParentId())
                .orElseThrow(ErrorCode.PARENT_FOLDER_NOT_FOUND.getException());

        // check acl of parent folder
        boolean createAllowed = new AuthorizationService().userHasPermission(parentFolder.getAclId(), DefaultPermission.CREATE_OBJECT, user);
        if (!createAllowed) {
            throw ErrorCode.NO_CREATE_PERMISSION.exception();
        }

        ObjectSystemData osd = new ObjectSystemData();
        osd.setParentId(parentFolder.getId());
        osd.setName(createRequest.getName());
        osd.setLatestHead(true);

        // check acl exists
        Long aclId = new AclDao().getObjectById(createRequest.getAclId()).orElseThrow(ErrorCode.ACL_NOT_FOUND.getException())
                .getId();
        osd.setAclId(aclId);

        // check owner exists
        Long ownerId = new UserAccountDao().getUserAccountById(createRequest.getOwnerId())
                .orElseThrow(ErrorCode.USER_ACCOUNT_NOT_FOUND.getException()).getId();
        osd.setOwnerId(ownerId);

        // check type exists
        Long typeId = new ObjectTypeDao().getObjectById(createRequest.getTypeId())
                .orElseThrow(ErrorCode.OBJECT_TYPE_NOT_FOUND.getException()).getId();
        osd.setTypeId(typeId);

        // check lifecycleState (if necessary)
        if (createRequest.getLifecycleStateId() != null) {
            Long lifecycleStateId = new LifecycleStateDao().getLifecycleStateById(createRequest.getLifecycleStateId())
                    .orElseThrow(ErrorCode.LIFECYCLE_STATE_NOT_FOUND.getException()).getId();
            osd.setLifecycleStateId(lifecycleStateId);
            // TODO: + enterState?
        }

        // check language if given
        final Long languageId;
        if (createRequest.getLanguageId() != null) {
            languageId = new LanguageDao().getObjectById(createRequest.getLanguageId())
                    .orElseThrow(ErrorCode.LANGUAGE_NOT_FOUND.getException()).getId();
        }
        else {
            languageId = new LanguageDao().getLanguageByIsoCode(LANGUAGE_UNDETERMINED_ISO_CODE)
                    .orElseThrow(ErrorCode.DB_IS_MISSING_LANGUAGE_CODE.getException()).getId();
        }
        osd.setLanguageId(languageId);

        osd.setCreatorId(user.getId());
        osd.setModifierId(user.getId());
        osd.setSummary(Objects.requireNonNullElse(createRequest.getSummary(), DEFAULT_SUMMARY));
        if (user.isChangeTracking()) {
            osd.setMetadataChanged(true);
        }
        osd = osdDao.saveOsd(osd);

        // handle custom metadata
        if (!createRequest.getMetas().isEmpty()) {
            var metas = createMetas(createRequest.getMetas(), osd.getId());
            osd.setMetas(metas);
        }

        Part file = request.getPart("file");
        if (file != null) {
            storeFileUpload(file.getInputStream(), osd, createRequest.getFormatId());
            if (user.isChangeTracking()) {
                osd.setContentChanged(true);
            }
            osdDao.updateOsd(osd, false);
        }
        OsdWrapper osdWrapper = new OsdWrapper(Collections.singletonList(osd));
        response.setWrapper(osdWrapper);
    }

    private List<Meta> createMetas(List<Meta> metas, Long id) {
        var dao = new OsdMetaDao();
        checkMetaUniqueness(metas);
        metas.forEach(meta -> meta.setObjectId(id));
        dao.create(metas);
        return dao.listByOsd(id);
    }

    private void deleteMeta(CinnamonRequest request, CinnamonResponse response, UserAccount user, OsdDao osdDao) throws
            IOException {
        DeleteMetaRequest metaRequest = (DeleteMetaRequest) getMapper().readValue(request.getInputStream(), DeleteMetaRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());

        OsdMetaDao osdMetaDao = new OsdMetaDao();
        List<Meta> metas      = osdMetaDao.getObjectsById(metaRequest.list());
        if (metas.size() != metaRequest.getIds().size() && !metaRequest.isIgnoreNotFound()) {
            throw ErrorCode.METASET_NOT_FOUND.exception();
        }
        new MetaService<>().deleteMetas(osdMetaDao, metas, osdDao, user);
        response.setResponse(new DeleteResponse(true));
    }

    /**
     * Note: getMeta allows OsdMetaRequests without metaset name to return all metasets.
     * This usage is deprecated.
     * Current Cinnamon 3 clients expect the arbitrary metaset XML to be added to the DOM of the metaset wrapper.
     * This requires assembling a new DOM tree in memory for each request to getMeta, which is not something you
     * want to see with large metasets.
     */
    private void getMeta(CinnamonRequest request, CinnamonResponse response, UserAccount user, OsdDao osdDao) throws
            IOException {
        MetaRequest metaRequest = xmlMapper.readValue(request.getInputStream(), MetaRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());

        Long             osdId = metaRequest.getId();
        ObjectSystemData osd   = osdDao.getObjectById(osdId).orElseThrow(ErrorCode.OBJECT_NOT_FOUND.getException());
        throwUnlessCustomMetaIsReadable(osd);

        List<Meta> metaList;
        if (metaRequest.getTypeIds() != null) {
            metaList = new OsdMetaDao().getMetaByTypeIdsAndOsd(metaRequest.getTypeIds(), osdId);
        }
        else {
            metaList = new OsdMetaDao().listByOsd(osdId);
        }

        createMetaResponse(response, metaList);
    }

    private void createMeta(CinnamonRequest request, CinnamonResponse response, UserAccount user, OsdDao osdDao) throws
            IOException {
        CreateMetaRequest metaRequest = xmlMapper.readValue(request.getInputStream(), CreateMetaRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());

        List<Meta> metas = new MetaService<>().createMeta(new OsdMetaDao(), metaRequest.getMetas(), osdDao, user);
        createMetaResponse(response, metas);
    }

    private void lock(CinnamonRequest request, CinnamonResponse response, UserAccount user, OsdDao osdDao) throws
            IOException {
        IdListRequest idRequest = xmlMapper.readValue(request.getInputStream(), IdListRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());
        List<ObjectSystemData> osds = osdDao.getObjectsById(idRequest.getIds());
        if (osds.size() != idRequest.getIds().size()) {
            throw ErrorCode.OBJECT_NOT_FOUND.exception();
        }

        List<CinnamonError> errors      = new ArrayList<>();
        boolean             isSuperuser = authorizationService.currentUserIsSuperuser();
        for (ObjectSystemData osd : osds) {
            boolean lockAllowed = authorizationService.hasUserOrOwnerPermission(osd, DefaultPermission.LOCK, user);
            if (!lockAllowed) {
                errors.add(new CinnamonError(ErrorCode.NO_LOCK_PERMISSION.getCode(), osd.getId()));
                continue;
            }
            Long lockHolder = osd.getLockerId();
            if (lockHolder != null) {
                if (lockHolder.equals(user.getId()) || isSuperuser) {
                    // trying to lock your own object: NOP
                    continue;
                }
                else {
                    errors.add(new CinnamonError(ErrorCode.OBJECT_LOCKED_BY_OTHER_USER.getCode(), osd.getId()));
                }
            }
        }
        if (errors.isEmpty() || isSuperuser) {
            osds.forEach(osd -> {
                osd.setLockerId(user.getId());
                osdDao.updateOsd(osd, false);
            });
            response.responseIsGenericOkay();
        }
        else {
            throw new FailedRequestException(ErrorCode.LOCK_FAILED, errors);
        }
    }

    private void unlock(CinnamonRequest request, CinnamonResponse response, UserAccount user, OsdDao osdDao) throws
            IOException {
        IdListRequest idRequest = xmlMapper.readValue(request.getInputStream(), IdListRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());
        List<ObjectSystemData> osds = osdDao.getObjectsById(idRequest.getIds());
        if (osds.size() != idRequest.getIds().size()) {
            throw ErrorCode.OBJECT_NOT_FOUND.exception();
        }
        List<CinnamonError> errors      = new ArrayList<>();
        boolean             isSuperuser = authorizationService.currentUserIsSuperuser();
        for (ObjectSystemData osd : osds) {
            boolean unlockAllowed = authorizationService.hasUserOrOwnerPermission(osd, DefaultPermission.LOCK, user);
            if (!unlockAllowed) {
                errors.add(new CinnamonError(ErrorCode.NO_LOCK_PERMISSION.getCode(), osd.getId()));
                continue;
            }
            Long lockHolder = osd.getLockerId();
            if (lockHolder != null) {
                // superuser may remove locks from other users.
                if (!lockHolder.equals(user.getId()) || isSuperuser) {
                    // trying to unlock another user's lock: nope.
                    errors.add(new CinnamonError(ErrorCode.OBJECT_LOCKED_BY_OTHER_USER.getCode(), osd.getId()));
                }
            }
        }
        if (errors.isEmpty() || isSuperuser) {
            osds.forEach(osd -> {
                osd.setLockerId(null);
                osdDao.updateOsd(osd, false);
            });
            response.responseIsGenericOkay();
        }
        else {
            throw new FailedRequestException(UNLOCK_FAILED, errors);
        }
        response.responseIsGenericOkay();

    }

    private void getContent(CinnamonRequest request, CinnamonResponse response, UserAccount user, OsdDao osdDao) throws
            ServletException, IOException {
        IdRequest idRequest = xmlMapper.readValue(request.getInputStream(), IdRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());
        ObjectSystemData osd = osdDao.getObjectById(idRequest.getId()).orElseThrow(ErrorCode.OBJECT_NOT_FOUND.getException());
        new AuthorizationService().throwUpUnlessUserOrOwnerHasPermission(osd, DefaultPermission.READ_OBJECT_CONTENT, user,
                ErrorCode.NO_READ_PERMISSION);
        if (osd.getContentSize() == null || osd.getContentSize() == 0) {
            throw ErrorCode.OBJECT_HAS_NO_CONTENT.exception();
        }
        Optional<Format> formatOpt = new FormatDao().getObjectById(osd.getFormatId());
        // no regular error response for missing format - this should only be possible if the database is corrupted.
        Format format = formatOpt.orElseThrow(
                () -> new ServletException(String.format("Encountered object #%d with content but non-existing formatId #%d.",
                        osd.getId(), osd.getFormatId())));
        ContentProvider contentProvider = contentProviderService.getContentProvider(osd.getContentProvider());
        InputStream     contentStream   = contentProvider.getContentStream(osd);
        response.setHeader(CONTENT_DISPOSITION, "attachment; filename=\"" + osd.getName().replace("\"", "%22") + "\"");
        response.setContentType(format.getContentType());
        response.setStatus(SC_OK);
        contentStream.transferTo(response.getOutputStream());
    }

    private void setContent(CinnamonRequest request, CinnamonResponse response, UserAccount user, OsdDao osdDao) throws
            ServletException, IOException {
        verifyIsMultipart(request);
        Part contentRequest = request.getPart(CINNAMON_REQUEST_PART);
        if (contentRequest == null) {
            throw MISSING_REQUEST_PAYLOAD.exception();
        }
        Part file = request.getPart("file");
        if (file == null) {
            throw ErrorCode.MISSING_FILE_PARAMETER.exception();
        }
        SetContentRequest setContentRequest = xmlMapper.readValue(contentRequest.getInputStream(), SetContentRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());
        ObjectSystemData osd          = osdDao.getObjectById(setContentRequest.getId()).orElseThrow(ErrorCode.OBJECT_NOT_FOUND.getException());
        boolean          writeAllowed = new AuthorizationService().hasUserOrOwnerPermission(osd, DefaultPermission.WRITE_OBJECT_CONTENT, user);
        if (!writeAllowed) {
            throw ErrorCode.NO_WRITE_PERMISSION.exception();
        }
        boolean isLocker = user.getId().equals(osd.getLockerId());
        if (!isLocker && !authorizationService.currentUserIsSuperuser()) {
            throw ErrorCode.OBJECT_MUST_BE_LOCKED_BY_USER.exception();
        }

        storeFileUpload(file.getInputStream(), osd, setContentRequest.getFormatId());
        if (user.isChangeTracking()) {
            osd.setContentChanged(true);
        }
        osdDao.updateOsd(osd, true);
        response.responseIsGenericOkay();
    }

    private void deleteTempFile(File tempFile) {
        boolean deleteResult = tempFile.delete();
        if (!deleteResult) {
            log.warn("Could not delete temporary upload file {}", tempFile.getAbsolutePath());
        }
    }

    private void storeFileUpload(InputStream inputStream, ObjectSystemData osd, Long formatId) throws IOException {
        FormatDao formatDao = new FormatDao();
        Format    format    = formatDao.getObjectById(formatId).orElseThrow(ErrorCode.FORMAT_NOT_FOUND.getException());

        // store file in tmp dir:
        Path tempFile       = Files.createTempFile("cinnamon-upload-", ".data");
        File tempOutputFile = tempFile.toFile();
        long bytesWritten   = Files.copy(inputStream, tempFile, REPLACE_EXISTING);

        // get content provider and store data:
        ContentProvider contentProvider = contentProviderService.getContentProvider(osd.getContentProvider());
        ContentMetadata metadata        = contentProvider.writeContentStream(osd, new FileInputStream(tempOutputFile));
        osd.setContentHash(metadata.getContentHash());
        osd.setContentPath(metadata.getContentPath());
        osd.setContentSize(metadata.getContentSize());
        osd.setFormatId(format.getId());
        tikaService.convertContentToTikaMetaset(osd, new FileInputStream(tempOutputFile), format);
        deleteTempFile(tempOutputFile);
    }

    private void setSummary(CinnamonRequest request, CinnamonResponse response, UserAccount user, OsdDao osdDao) throws
            IOException {
        SetSummaryRequest summaryRequest = xmlMapper.readValue(request.getInputStream(), SetSummaryRequest.class);
        ObjectSystemData  osd            = osdDao.getObjectById(summaryRequest.getId()).orElseThrow(ErrorCode.OBJECT_NOT_FOUND.getException());
        authorizationService.throwUpUnlessUserOrOwnerHasPermission(osd, DefaultPermission.SET_SUMMARY, user,
                ErrorCode.NO_SET_SUMMARY_PERMISSION);
        osd.setSummary(summaryRequest.getSummary());
        osdDao.updateOsd(osd, true);
        response.responseIsGenericOkay();
    }

    private void getSummaries(CinnamonRequest request, CinnamonResponse response, UserAccount user, OsdDao
            osdDao) throws IOException {
        IdListRequest          idListRequest = xmlMapper.readValue(request.getInputStream(), IdListRequest.class);
        SummaryWrapper         wrapper       = new SummaryWrapper();
        List<ObjectSystemData> osds          = osdDao.getObjectsById(idListRequest.getIds().stream().toList(), true);
        osds.forEach(osd -> {
            if (authorizationService.hasUserOrOwnerPermission(osd, DefaultPermission.BROWSE, user)) {
                wrapper.getSummaries().add(new Summary(osd.getId(), osd.getSummary()));
            }
        });
        response.setWrapper(wrapper);
    }

    private void update(CinnamonRequest request, CinnamonResponse response, UserAccount user, OsdDao
            osdDao) throws IOException {
        UpdateOsdRequest updateRequest = xmlMapper.readValue(request.getInputStream(), UpdateOsdRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());
        List<ObjectSystemData> osds = osdDao.getObjectsById(updateRequest.getOsds().stream().map(ObjectSystemData::getId).toList());
        if (osds.size() != updateRequest.getOsds().size()) {
            throw ErrorCode.OBJECT_NOT_FOUND.exception();
        }
        boolean                     isAdmin    = authorizationService.currentUserIsSuperuser();
        Map<Long, ObjectSystemData> updateOsds = updateRequest.getOsds().stream().collect(Collectors.toMap(ObjectSystemData::getId, k -> k));
        for (ObjectSystemData osd : osds) {
            ObjectSystemData update = updateOsds.get(osd.getId());
            if (osd.lockedByOtherUser(user) && !isAdmin) {
                throw ErrorCode.OBJECT_LOCKED_BY_OTHER_USER.getException().get();
            }
            if (!osd.lockedByUser(user) && !isAdmin) {
                throw ErrorCode.OBJECT_MUST_BE_LOCKED_BY_USER.getException().get();
            }

            AccessFilter accessFilter = AccessFilter.getInstance(user);
            FolderDao    folderDao    = new FolderDao();

            boolean changed = false;
            // change parent folder
            Long parentId = update.getParentId();
            if (parentId != null) {
                Folder parentFolder = folderDao.getFolderById(parentId)
                        .orElseThrow(ErrorCode.PARENT_FOLDER_NOT_FOUND.getException());
                if (!accessFilter.hasPermissionOnOwnable(parentFolder, DefaultPermission.CREATE_OBJECT, parentFolder)) {
                    ErrorCode.NO_CREATE_PERMISSION.throwUp();
                }
                if (!accessFilter.hasPermissionOnOwnable(osd, DefaultPermission.SET_PARENT, osd)) {
                    ErrorCode.NO_SET_PARENT_PERMISSION.throwUp();
                }
                osd.setParentId(parentFolder.getId());
                changed = true;
            }

            // change name
            String name = update.getName();
            if (name != null) {
                if (!accessFilter.hasPermissionOnOwnable(osd, DefaultPermission.SET_NAME, osd)) {
                    throw ErrorCode.NO_NAME_WRITE_PERMISSION.exception();
                }
                osd.setName(name);
                changed = true;
            }

            // change type
            Long typeId = update.getTypeId();
            if (typeId != null) {
                if (!accessFilter.hasPermissionOnOwnable(osd, DefaultPermission.SET_TYPE, osd)) {
                    throw NO_TYPE_WRITE_PERMISSION.exception();
                }
                ObjectType type = new ObjectTypeDao().getObjectById(typeId)
                        .orElseThrow(ErrorCode.OBJECT_TYPE_NOT_FOUND.getException());
                osd.setTypeId(type.getId());
                changed = true;
            }

            // change acl
            Long aclId = update.getAclId();
            if (aclId != null) {
                if (!accessFilter.hasPermissionOnOwnable(osd, DefaultPermission.SET_ACL, osd)) {
                    ErrorCode.MISSING_SET_ACL_PERMISSION.throwUp();
                }
                Acl acl = new AclDao().getObjectById(aclId)
                        .orElseThrow(ErrorCode.ACL_NOT_FOUND.getException());
                osd.setAclId(acl.getId());
                changed = true;
            }

            // change owner
            Long ownerId = update.getOwnerId();
            if (ownerId != null) {
                if (!accessFilter.hasPermissionOnOwnable(osd, DefaultPermission.SET_OWNER, osd)) {
                    NO_SET_OWNER_PERMISSION.throwUp();
                }
                UserAccount owner = new UserAccountDao().getUserAccountById(ownerId)
                        .orElseThrow(ErrorCode.USER_ACCOUNT_NOT_FOUND.getException());
                osd.setOwnerId(owner.getId());
                changed = true;
            }

            // change language
            Long languageId = update.getLanguageId();
            if (languageId != null) {
                if (!accessFilter.hasPermissionOnOwnable(osd, DefaultPermission.SET_LANGUAGE, osd)) {
                    NO_UPDATE_LANGUAGE_PERMISSION.throwUp();
                }
                Language language = new LanguageDao().getObjectById(languageId)
                        .orElseThrow(ErrorCode.LANGUAGE_NOT_FOUND.getException());
                osd.setLanguageId(language.getId());
                changed = true;
            }

            // contentChanged
            if (updateRequest.isUpdateContentChanged()) {
                if (user.isChangeTracking()) {
                    throw ErrorCode.CHANGED_FLAG_ONLY_USABLE_BY_UNTRACKED_USERS.exception();
                }
                osd.setContentChanged(update.isContentChanged());
            }

            // metadataChanged
            if (updateRequest.isUpdateMetadataChanged()) {
                if (user.isChangeTracking()) {
                    throw ErrorCode.CHANGED_FLAG_ONLY_USABLE_BY_UNTRACKED_USERS.exception();
                }
                osd.setMetadataChanged(update.isMetadataChanged());
                changed = true;
            }
            else {
                if (user.isChangeTracking()) {
                    osd.setMetadataChanged(true);
                }
            }

            // update osd:
            if (changed) {
                osdDao.updateOsd(osd, true);
            }
        }
        response.responseIsGenericOkay();
    }

    private void checkPermissionAndAddCustomMetadata(List<ObjectSystemData> osds, UserAccount user) {
        OsdMetaDao metaDao = new OsdMetaDao();
        boolean hasReadMetaPermission = osds.stream().allMatch(osd ->
                authorizationService.hasUserOrOwnerPermission(osd, DefaultPermission.READ_OBJECT_CUSTOM_METADATA, user)
        );
        if (!hasReadMetaPermission) {
            ErrorCode.NO_READ_CUSTOM_METADATA_PERMISSION.throwUp();
            return;
        }
        osds.forEach(osd -> osd.setMetas(metaDao.listByOsd(osd.getId())));

    }

    private void getObjectsById(CinnamonRequest request, CinnamonResponse response, UserAccount user, OsdDao
            osdDao) throws IOException {
        OsdRequest osdRequest = xmlMapper.readValue(request.getInputStream(), OsdRequest.class);
        if (!osdRequest.validated()) {
            log.debug("invalid osdRequest - check list of ids (must be non-empty list of positive integers):\n{}",
                    getMapper().writeValueAsString(osdRequest));
            throw ErrorCode.INVALID_REQUEST.exception();
        }
        List<ObjectSystemData> osds         = osdDao.getObjectsById(osdRequest.getIds(), osdRequest.isIncludeSummary());
        List<ObjectSystemData> filteredOsds = authorizationService.filterObjectsByBrowsePermission(osds, user);

        if (osdRequest.isIncludeCustomMetadata()) {
            checkPermissionAndAddCustomMetadata(filteredOsds, user);
        }

        OsdWrapper wrapper = new OsdWrapper();
        wrapper.setOsds(filteredOsds);
        response.setWrapper(wrapper);
    }

    private void getObjectsByFolderId(CinnamonRequest request, CinnamonResponse response, UserAccount
            user, OsdDao osdDao) throws IOException {
        OsdByFolderRequest     osdRequest     = xmlMapper.readValue(request.getInputStream(), OsdByFolderRequest.class);
        long                   folderId       = osdRequest.getFolderId();
        boolean                includeSummary = osdRequest.isIncludeSummary();
        boolean                includeMeta    = osdRequest.isIncludeCustomMetadata();
        List<ObjectSystemData> osds           = osdDao.getObjectsByFolderId(folderId, includeSummary, osdRequest.getVersionPredicate());
        List<ObjectSystemData> filteredOsds   = authorizationService.filterObjectsByBrowsePermission(osds, user);
        OsdMetaDao             metaDao        = new OsdMetaDao();
        if (includeMeta) {
            checkPermissionAndAddCustomMetadata(filteredOsds, user);
            filteredOsds.forEach(osd -> osd.setMetas(metaDao.listByOsd(osd.getId())));
        }
        LinkDao    linkDao       = new LinkDao();
        List<Link> links         = linkDao.getLinksByFolderIdAndLinkType(List.of(folderId), LinkType.OBJECT);
        List<Link> filteredLinks = authorizationService.filterOsdLinksAndTargetsByBrowsePermission(links, user);

        OsdWrapper wrapper = new OsdWrapper();
        wrapper.setOsds(filteredOsds);
        wrapper.setLinks(filteredLinks);
        if (osdRequest.isLinksAsOsd()) {
            List<Long>             resolvedIds = filteredLinks.stream().map(Link::resolveLink).filter(Objects::nonNull).toList();
            List<ObjectSystemData> references  = osdDao.getObjectsById(resolvedIds, includeSummary);
            if (includeMeta) {
                references.forEach(osd -> osd.setMetas(metaDao.listByOsd(osd.getId())));
            }
            wrapper.setReferences(references);
        }

        response.setWrapper(wrapper);
    }

    private void newVersion(CinnamonRequest request, CinnamonResponse response, UserAccount user, OsdDao osdDao) throws
            ServletException, IOException {
        verifyIsMultipart(request);
        Part contentRequest = request.getPart(CINNAMON_REQUEST_PART);
        CreateNewVersionRequest versionRequest = xmlMapper.readValue(contentRequest.getInputStream(), CreateNewVersionRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());
        ObjectSystemData preOsd    = osdDao.getObjectById(versionRequest.getId()).orElseThrow(ErrorCode.OBJECT_NOT_FOUND.getException());
        FolderDao        folderDao = new FolderDao();
        Folder           folder    = folderDao.getFolderById(preOsd.getParentId()).orElseThrow(FOLDER_NOT_FOUND::exception);
        authorizationService.throwUpUnlessUserOrOwnerHasPermission(folder, DefaultPermission.CREATE_OBJECT, user,
                NO_CREATE_PERMISSION);
        authorizationService.throwUpUnlessUserOrOwnerHasPermission(preOsd, DefaultPermission.VERSION_OBJECT, user,
                NO_VERSION_PERMISSION);

        Optional<String> lastDescendantVersion = osdDao.findLastDescendantVersion(preOsd.getId());
        ObjectSystemData osd                   = preOsd.createNewVersion(user, lastDescendantVersion.orElse(null));

        /*
         * fixLatestHeadAndBranch:
         * When a new version is created, the predecessor is no longer the latest version in head or branch:
         */
        preOsd.setLatestBranch(false);
        preOsd.setLatestHead(false);
        // TODO: probably set latestBranch on latestDescendant to false, too.
        osdDao.updateOsd(preOsd, false);

        if (osd.getCmnVersion().matches("^\\d+$")) {
            osd.setLatestHead(true);
        }
        // save here to generate id so metasets can be created and linked to the OSD.
        ObjectSystemData savedOsd = osdDao.saveOsd(osd);

        // copy relations
        RelationDao    relationDao = new RelationDao();
        List<Relation> relations   = relationDao.getRelationsToCopyOnVersion(preOsd.getId());
        Map<Long, RelationType> relationTypes = new RelationTypeDao()
                .getRelationTypeMap(relations.stream().map(Relation::getTypeId).collect(Collectors.toSet()));
        List<Relation> relationCopies = new ArrayList<>();
        for (Relation relation : relations) {
            RelationType relationType = relationTypes.get(relation.getTypeId());
            if (relationType.isCloneOnLeftVersion() && relation.getLeftId().equals(preOsd.getId())) {
                Relation rel = new Relation(savedOsd.getId(), relation.getRightId(), relation.getTypeId(), relation.getMetadata());
                relationCopies.add(rel);
            }
            if (relationType.isCloneOnRightVersion() && relation.getRightId().equals(preOsd.getId())) {
                Relation rel = new Relation(relation.getLeftId(), savedOsd.getId(), relation.getTypeId(), relation.getMetadata());
                relationCopies.add(rel);
            }
        }
        relationDao.create(relationCopies);

        // storeMetadata
        if (versionRequest.hasMetaRequests()) {
            OsdMetaDao            osdMetaDao = new OsdMetaDao();
            final List<ErrorCode> errorCodes = new ArrayList<>();
            versionRequest.getMetaRequests().forEach(metadata -> {
                        try {
                            MetasetType metasetType = new MetasetTypeDao().getMetasetTypeById(metadata.getTypeId())
                                    .orElseThrow(ErrorCode.METASET_TYPE_NOT_FOUND.getException());
                            Meta meta = new Meta(osd.getId(), metasetType.getId(), metadata.getContent());
                            osdMetaDao.create(List.of(meta));
                        } catch (FailedRequestException e) {
                            errorCodes.add(e.getErrorCode());
                        } catch (Exception e) {
                            // TODO: should not handle unknown exceptions - those are probably bugs.
                            errorCodes.add(ErrorCode.INTERNAL_SERVER_ERROR_TRY_AGAIN_LATER);
                        }
                    }
            );
            if (errorCodes.size() > 0) {
                // for now, just throw the first error.
                throw errorCodes.get(0).exception();
            }
            if (user.isChangeTracking()) {
                osd.setMetadataChanged(true);
            }
        }

        // saveFileUpload
        boolean shouldUpdateTikaMetaset = false;
        Part    file                    = request.getPart("file");
        if (file != null) {
            storeFileUpload(file.getInputStream(), osd, versionRequest.getFormatId());
            shouldUpdateTikaMetaset = true;
            if (user.isChangeTracking()) {
                osd.setContentChanged(true);
            }
        }

        // set lifecycle state
        if (preOsd.getLifecycleStateId() != null) {
            LifecycleStateDao stateDao = new LifecycleStateDao();
            LifecycleState lifecycleState = stateDao
                    .getLifecycleStateById(preOsd.getLifecycleStateId())
                    .orElseThrow(ErrorCode.LIFECYCLE_STATE_NOT_FOUND.getException());
            Long copyId = lifecycleState.getLifecycleStateForCopyId();
            if (copyId == null) {
                log.debug("lifecycleStateForCopy is not set on OSD {}", osd.getId());
                osd.setLifecycleStateId(null);
            }
            else {
                LifecycleState stateForCopy = stateDao.getLifecycleStateById(copyId)
                        .orElseThrow(LIFECYCLE_STATE_NOT_FOUND.getException());
                State             stateImpl         = StateProviderService.getInstance().getStateProvider(stateForCopy.getStateClass()).getState();
                StateChangeResult attachStateResult = stateImpl.enter(osd, stateForCopy.getLifecycleStateConfig());
                if (!attachStateResult.isSuccessful()) {
                    throw ErrorCode.LIFECYCLE_STATE_CHANGE_FAILED.exception();
                }
                osd.setLifecycleStateId(stateForCopy.getId());
            }
        }
        osdDao.updateOsd(osd, false, shouldUpdateTikaMetaset);
        OsdWrapper wrapper = new OsdWrapper();
        wrapper.setOsds(Collections.singletonList(savedOsd));
        response.setWrapper(wrapper);
    }

    private void verifyIsMultipart(HttpServletRequest request) {
        String contentType = Optional.ofNullable(request.getContentType())
                .orElseThrow(ErrorCode.NO_CONTENT_TYPE_IN_HEADER.getException());
        if (!contentType.toLowerCase().startsWith(MULTIPART)) {
            throw ErrorCode.NOT_MULTIPART_UPLOAD.exception();
        }
    }

    @Override
    public ObjectMapper getMapper() {
        return xmlMapper;
    }

    @Override
    public void init() {
        tikaService            = ((TikaService) getServletContext().getAttribute(TIKA_SERVICE));
        contentProviderService = (ContentProviderService) getServletContext().getAttribute(CONTENT_PROVIDER_SERVICE);
    }
}
