package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.DefaultPermission;
import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.FailedRequestException;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.api.content.ContentMetadata;
import com.dewarim.cinnamon.api.content.ContentProvider;
import com.dewarim.cinnamon.api.lifecycle.State;
import com.dewarim.cinnamon.api.lifecycle.StateChangeResult;
import com.dewarim.cinnamon.application.CinnamonResponse;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.application.exception.CinnamonException;
import com.dewarim.cinnamon.application.service.DeleteOsdService;
import com.dewarim.cinnamon.application.service.MetaService;
import com.dewarim.cinnamon.application.service.TikaService;
import com.dewarim.cinnamon.dao.AclDao;
import com.dewarim.cinnamon.dao.FolderDao;
import com.dewarim.cinnamon.dao.FormatDao;
import com.dewarim.cinnamon.dao.LanguageDao;
import com.dewarim.cinnamon.dao.LifecycleStateDao;
import com.dewarim.cinnamon.dao.LinkDao;
import com.dewarim.cinnamon.dao.MetasetTypeDao;
import com.dewarim.cinnamon.dao.ObjectTypeDao;
import com.dewarim.cinnamon.dao.OsdDao;
import com.dewarim.cinnamon.dao.OsdMetaDao;
import com.dewarim.cinnamon.dao.RelationDao;
import com.dewarim.cinnamon.dao.RelationTypeDao;
import com.dewarim.cinnamon.dao.UserAccountDao;
import com.dewarim.cinnamon.model.Acl;
import com.dewarim.cinnamon.model.Folder;
import com.dewarim.cinnamon.model.Format;
import com.dewarim.cinnamon.model.IndexMode;
import com.dewarim.cinnamon.model.Language;
import com.dewarim.cinnamon.model.LifecycleState;
import com.dewarim.cinnamon.model.Meta;
import com.dewarim.cinnamon.model.MetasetType;
import com.dewarim.cinnamon.model.ObjectSystemData;
import com.dewarim.cinnamon.model.ObjectType;
import com.dewarim.cinnamon.model.UserAccount;
import com.dewarim.cinnamon.model.links.Link;
import com.dewarim.cinnamon.model.relations.Relation;
import com.dewarim.cinnamon.model.relations.RelationType;
import com.dewarim.cinnamon.model.request.CreateMetaRequest;
import com.dewarim.cinnamon.model.request.CreateNewVersionRequest;
import com.dewarim.cinnamon.model.request.DeleteAllMetasRequest;
import com.dewarim.cinnamon.model.request.DeleteMetaRequest;
import com.dewarim.cinnamon.model.request.IdListRequest;
import com.dewarim.cinnamon.model.request.IdRequest;
import com.dewarim.cinnamon.model.request.MetaRequest;
import com.dewarim.cinnamon.model.request.SetSummaryRequest;
import com.dewarim.cinnamon.model.request.UpdateMetaRequest;
import com.dewarim.cinnamon.model.request.osd.CopyOsdRequest;
import com.dewarim.cinnamon.model.request.osd.CreateOsdRequest;
import com.dewarim.cinnamon.model.request.osd.DeleteOsdRequest;
import com.dewarim.cinnamon.model.request.osd.GetRelationsRequest;
import com.dewarim.cinnamon.model.request.osd.OsdByFolderRequest;
import com.dewarim.cinnamon.model.request.osd.OsdRequest;
import com.dewarim.cinnamon.model.request.osd.SetContentRequest;
import com.dewarim.cinnamon.model.request.osd.UpdateOsdRequest;
import com.dewarim.cinnamon.model.response.DeleteResponse;
import com.dewarim.cinnamon.model.response.GenericResponse;
import com.dewarim.cinnamon.model.response.OsdWrapper;
import com.dewarim.cinnamon.model.response.RelationWrapper;
import com.dewarim.cinnamon.model.response.Summary;
import com.dewarim.cinnamon.model.response.SummaryWrapper;
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
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.dewarim.cinnamon.ErrorCode.*;
import static com.dewarim.cinnamon.api.Constants.*;
import static jakarta.servlet.http.HttpServletResponse.SC_OK;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.apache.http.entity.mime.MIME.CONTENT_DISPOSITION;

@MultipartConfig
@WebServlet(name = "Osd", urlPatterns = "/")
public class OsdServlet extends BaseServlet implements CruddyServlet<ObjectSystemData> {

    private final        ObjectMapper         xmlMapper            = XML_MAPPER;
    private final        AuthorizationService authorizationService = new AuthorizationService();
    private final        DeleteOsdService     deleteOsdService     = new DeleteOsdService();
    private static final Logger               log                  = LogManager.getLogger(OsdServlet.class);
    private final        Long                 tikaMetasetTypeId;
    private              TikaService          tikaService;

    public OsdServlet() {
        super();
        Optional<MetasetType> tikaMetasetType = new MetasetTypeDao().list().stream().filter(meta -> meta.getName().equals(TIKA_METASET_NAME)).findFirst();
        tikaMetasetTypeId = tikaMetasetType.map(MetasetType::getId).orElse(null);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        UserAccount user   = ThreadLocalSqlSession.getCurrentUser();
        OsdDao      osdDao = new OsdDao();

        CinnamonResponse cinnamonResponse = (CinnamonResponse) response;
        UrlMapping       mapping          = UrlMapping.getByPath(request.getRequestURI());
        switch (mapping) {
            case OSD__COPY -> copyOsd(request, cinnamonResponse, user, osdDao);
            case OSD__CREATE_OSD -> createOsd(request, cinnamonResponse, user, osdDao);
            case OSD__CREATE_META -> createMeta(request, cinnamonResponse, user, osdDao);
            case OSD__DELETE -> deleteOsds(request, cinnamonResponse, user, osdDao);
            case OSD__DELETE_META -> deleteMeta(request, cinnamonResponse, user, osdDao);
            case OSD__DELETE_ALL_METAS -> deleteAllMetas(request, cinnamonResponse, user, osdDao);
            case OSD__GET_CONTENT -> getContent(request, cinnamonResponse, user, osdDao);
            case OSD__GET_META -> getMeta(request, cinnamonResponse, user, osdDao);
            case OSD__GET_OBJECTS_BY_FOLDER_ID -> getObjectsByFolderId(request, cinnamonResponse, user, osdDao);
            case OSD__GET_OBJECTS_BY_ID -> getObjectsById(request, cinnamonResponse, user, osdDao);
            case OSD__GET_RELATIONS -> getRelations(request, cinnamonResponse, user, osdDao);
            case OSD__GET_SUMMARIES -> getSummaries(request, cinnamonResponse, user, osdDao);
            case OSD__LOCK -> lock(request, cinnamonResponse, user, osdDao);
            case OSD__SET_CONTENT -> setContent(request, cinnamonResponse, user, osdDao);
            case OSD__SET_SUMMARY -> setSummary(request, cinnamonResponse, user, osdDao);
            case OSD__UNLOCK -> unlock(request, cinnamonResponse, user, osdDao);
            case OSD__UPDATE -> update(request, cinnamonResponse, user, osdDao);
            case OSD__UPDATE_META_CONTENT -> updateMetaContent(request, cinnamonResponse, user, osdDao);
            case OSD__VERSION -> newVersion(request, cinnamonResponse, user, osdDao);
            default -> ErrorCode.RESOURCE_NOT_FOUND.throwUp();
        }
    }

    private void updateMetaContent(HttpServletRequest request, CinnamonResponse cinnamonResponse, UserAccount user, OsdDao osdDao) throws IOException {
        UpdateMetaRequest metaRequest = (UpdateMetaRequest) getMapper().readValue(request.getInputStream(), UpdateMetaRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());
        OsdMetaDao osdMetaDao = new OsdMetaDao();
        new MetaService<>().updateMeta(osdMetaDao, metaRequest.getMetas(), osdDao, user);
        cinnamonResponse.setResponse(new GenericResponse(true));
    }

    private void deleteAllMetas(HttpServletRequest request, CinnamonResponse cinnamonResponse, UserAccount user, OsdDao osdDao) throws IOException {
        DeleteAllMetasRequest metaRequest = (DeleteAllMetasRequest) getMapper().readValue(request.getInputStream(), DeleteAllMetasRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());

        OsdMetaDao metaDao = new OsdMetaDao();
        List<Meta> metas   = metaDao.listMetaByObjectIds(metaRequest.getIds());
        new MetaService<>().deleteMetas(metaDao, metas, osdDao, user);
        cinnamonResponse.setWrapper(new DeleteResponse(true));
    }

    private void getRelations(HttpServletRequest request, CinnamonResponse cinnamonResponse, UserAccount user, OsdDao osdDao) throws IOException {
        GetRelationsRequest relationRequest = xmlMapper.readValue(request.getInputStream(), GetRelationsRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());

        var                    accessFilter = AccessFilter.getInstance(user);
        List<ObjectSystemData> osds         = osdDao.getObjectsById(relationRequest.getIds(), false);
        boolean hasReadSysMetaPermission = osds.stream().allMatch(osd ->
                accessFilter.hasPermissionOnOwnable(osd, DefaultPermission.READ_OBJECT_SYS_METADATA, osd));
        if (!hasReadSysMetaPermission) {
            ErrorCode.NO_READ_OBJECT_SYS_METADATA_PERMISSION.throwUp();
            return;
        }
        List<Long>      ids             = osds.stream().map(ObjectSystemData::getId).collect(Collectors.toList());
        List<Relation>  relations       = new RelationDao().getRelationsOrMode(ids, ids, null, relationRequest.isIncludeMetadata());
        RelationWrapper relationWrapper = new RelationWrapper(relations);
        cinnamonResponse.setWrapper(relationWrapper);
    }

    private void copyOsd(HttpServletRequest request, CinnamonResponse cinnamonResponse, UserAccount user, OsdDao osdDao) throws IOException {
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
                accessFilter.hasPermissionOnOwnable(osd, DefaultPermission.READ_OBJECT_SYS_METADATA, osd));
        if (!hasReadSysMetaPermission) {
            ErrorCode.NO_READ_OBJECT_SYS_METADATA_PERMISSION.throwUp();
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
                metasetDao.create(metaCopies);
            }

            // copy content
            if (osd.getContentHash() != null) {
                log.debug("copy content for osd#" + id);
                ContentProvider contentProvider = ContentProviderService.getInstance().getContentProvider(osd.getContentProvider());
                try (InputStream contentStream = contentProvider.getContentStream(osd)) {
                    ContentMetadata metadata = contentProvider.writeContentStream(copy, contentStream);
                    copy.setContentHash(metadata.getContentHash());
                    copy.setContentPath(metadata.getContentPath());
                    copy.setContentSize(metadata.getContentSize());
                    copy.setContentProvider(contentProvider.getName());
                    copy.setFormatId(osd.getFormatId());
                    Format format = new FormatDao().getObjectById(osd.getFormatId()).orElseThrow();
                    convertContentToTikaMetaset(osd, contentProvider.getContentStream(metadata), format);
                    osdDao.updateOsd(copy, false);
                }
            }
            copies.add(copy);
        }

        cinnamonResponse.setWrapper(new OsdWrapper(copies));
    }


    private void deleteOsds(HttpServletRequest request, CinnamonResponse cinnamonResponse, UserAccount user, OsdDao osdDao) throws IOException {
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


    private void createOsd(HttpServletRequest request, CinnamonResponse response, UserAccount user, OsdDao osdDao) throws
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
        Long typeId = new ObjectTypeDao().getObjectTypeById(createRequest.getTypeId())
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
            languageId = new LanguageDao().getLanguageById(createRequest.getLanguageId())
                    .orElseThrow(ErrorCode.LANGUAGE_NOT_FOUND.getException()).getId();
        } else {
            languageId = new LanguageDao().getLanguageByIsoCode(LANGUAGE_UNDETERMINED_ISO_CODE)
                    .orElseThrow(ErrorCode.DB_IS_MISSING_LANGUAGE_CODE.getException()).getId();
        }
        osd.setLanguageId(languageId);

        osd.setCreatorId(user.getId());
        osd.setModifierId(user.getId());
        osd.setSummary(Objects.requireNonNullElse(createRequest.getSummary(), DEFAULT_SUMMARY));

        osd = osdDao.saveOsd(osd);

        // handle custom metadata
        if (!createRequest.getMetas().isEmpty()) {
            var metas = createMetas(createRequest.getMetas(), osd.getId());
            osd.setMetas(metas);
        }

        Part file = request.getPart("file");
        if (file != null) {
            storeFileUpload(file.getInputStream(), osd, createRequest.getFormatId());
            osdDao.updateOsd(osd, false);
        }
        OsdWrapper osdWrapper = new OsdWrapper(Collections.singletonList(osd));
        response.setWrapper(osdWrapper);
    }

    private List<Meta> createMetas(List<Meta> metas, Long id) {
        var dao = new OsdMetaDao();
        checkMetaUniqueness(metas);
        dao.create(metas.stream().peek(meta -> meta.setObjectId(id)).collect(Collectors.toList()));
        return dao.listByOsd(id);
    }

    private void checkMetaUniqueness(List<Meta> metas) {
        Set<Long> uniqueTypes = new MetasetTypeDao().list().stream().filter(MetasetType::getUnique).map(MetasetType::getId).collect(Collectors.toSet());
        boolean multipleUniques = metas.stream().map(Meta::getTypeId).anyMatch(typeId ->
                uniqueTypes.contains(typeId) && metas.stream().filter(meta -> meta.getTypeId().equals(typeId)).count() > 1
        );
        if (multipleUniques) {
            throw ErrorCode.METASET_UNIQUE_CHECK_FAILED.exception();
        }
    }

    private void deleteMeta(HttpServletRequest request, CinnamonResponse response, UserAccount user, OsdDao osdDao) throws
            IOException {
        DeleteMetaRequest metaRequest = (DeleteMetaRequest) getMapper().readValue(request.getInputStream(), DeleteMetaRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());

        OsdMetaDao osdMetaDao = new OsdMetaDao();
        List<Meta> metas      = osdMetaDao.getObjectsById(metaRequest.getIds());
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
    private void getMeta(HttpServletRequest request, CinnamonResponse response, UserAccount user, OsdDao osdDao) throws
            IOException {
        MetaRequest metaRequest = xmlMapper.readValue(request.getInputStream(), MetaRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());

        Long             osdId = metaRequest.getId();
        ObjectSystemData osd   = osdDao.getObjectById(osdId).orElseThrow(ErrorCode.OBJECT_NOT_FOUND.getException());
        throwUnlessCustomMetaIsReadable(osd);

        List<Meta> metaList;
        if (metaRequest.getTypeIds() != null) {
            metaList = new OsdMetaDao().getMetaByTypeIdsAndOsd(metaRequest.getTypeIds(), osdId);
        } else {
            metaList = new OsdMetaDao().listByOsd(osdId);
        }

        createMetaResponse(response, metaList);
    }

    private void createMeta(HttpServletRequest request, CinnamonResponse response, UserAccount user, OsdDao osdDao) throws
            IOException {
        CreateMetaRequest metaRequest = xmlMapper.readValue(request.getInputStream(), CreateMetaRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());

        List<Meta> metas = new MetaService<>().createMeta(new OsdMetaDao(), metaRequest.getMetas(), osdDao, user);
        createMetaResponse(response, metas);
    }

    private MetasetType determineMetasetType(Long typeId, String typeName) {
        if (typeId != null) {
            return new MetasetTypeDao().getMetasetTypeById(typeId)
                    .orElseThrow(ErrorCode.METASET_TYPE_NOT_FOUND.getException());
        } else {
            return new MetasetTypeDao().getMetasetTypeByName(typeName)
                    .orElseThrow(ErrorCode.METASET_TYPE_NOT_FOUND.getException());
        }
    }

    private void lock(HttpServletRequest request, CinnamonResponse response, UserAccount user, OsdDao osdDao) throws
            IOException {
        IdRequest idRequest = xmlMapper.readValue(request.getInputStream(), IdRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());

        ObjectSystemData osd         = osdDao.getObjectById(idRequest.getId()).orElseThrow(ErrorCode.OBJECT_NOT_FOUND.getException());
        boolean          lockAllowed = new AuthorizationService().hasUserOrOwnerPermission(osd, DefaultPermission.LOCK, user);
        if (!lockAllowed) {
            throw ErrorCode.NO_LOCK_PERMISSION.exception();
        }
        Long lockHolder = osd.getLockerId();
        if (lockHolder != null) {
            if (lockHolder.equals(user.getId())) {
                // trying to lock your own object: NOP
                response.responseIsGenericOkay();
                return;
            } else {
                throw ErrorCode.OBJECT_LOCKED_BY_OTHER_USER.exception();
            }
        }
        osd.setLockerId(user.getId());
        osdDao.updateOsd(osd, false);
        response.responseIsGenericOkay();
    }


    private void unlock(HttpServletRequest request, CinnamonResponse response, UserAccount user, OsdDao osdDao) throws
            IOException {
        IdRequest idRequest = xmlMapper.readValue(request.getInputStream(), IdRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());
        ObjectSystemData osd = osdDao.getObjectById(idRequest.getId()).orElseThrow(ErrorCode.OBJECT_NOT_FOUND.getException());
        new AuthorizationService().throwUpUnlessUserOrOwnerHasPermission(osd, DefaultPermission.LOCK, user,
                ErrorCode.NO_LOCK_PERMISSION);

        Long lockHolder = osd.getLockerId();
        if (lockHolder != null) {
            UserAccountDao userDao = new UserAccountDao();
            // superuser may remove locks from other users.
            if (lockHolder.equals(user.getId()) || userDao.isSuperuser(user)) {
                osd.setLockerId(null);
                osdDao.updateOsd(osd, false);
                response.responseIsGenericOkay();
            } else {
                // trying to unlock another user's lock: nope.
                throw ErrorCode.OBJECT_LOCKED_BY_OTHER_USER.exception();
            }
        } else {
            // trying to unlock an unlocked object: NOP
            response.responseIsGenericOkay();
        }
    }

    private void getContent(HttpServletRequest request, CinnamonResponse response, UserAccount user, OsdDao osdDao) throws
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
        ContentProvider contentProvider = ContentProviderService.getInstance().getContentProvider(osd.getContentProvider());
        InputStream     contentStream   = contentProvider.getContentStream(osd);
        response.setHeader(CONTENT_DISPOSITION, "attachment; filename=\"" + osd.getName().replace("\"", "%22") + "\"");
        response.setContentType(format.getContentType());
        response.setStatus(SC_OK);
        contentStream.transferTo(response.getOutputStream());
    }

    private void setContent(HttpServletRequest request, CinnamonResponse response, UserAccount user, OsdDao osdDao) throws
            ServletException, IOException {
        verifyIsMultipart(request);
        Part contentRequest = request.getPart(CINNAMON_REQUEST_PART);
        if (contentRequest == null) {
            throw ErrorCode.INVALID_REQUEST.exception();
        }
        Part file = request.getPart(("file"));
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
        if (!isLocker) {
            throw ErrorCode.OBJECT_MUST_BE_LOCKED_BY_USER.exception();
        }

        storeFileUpload(file.getInputStream(), osd, setContentRequest.getFormatId());
        osdDao.updateOsd(osd, true);
        response.responseIsGenericOkay();
    }

    private void deleteTempFile(File tempFile) {
        boolean deleteResult = tempFile.delete();
        if (!deleteResult) {
            log.warn("Could not delete temporary upload file " + tempFile.getAbsolutePath());
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
        ContentProvider contentProvider = ContentProviderService.getInstance().getContentProvider(osd.getContentProvider());
        ContentMetadata metadata        = contentProvider.writeContentStream(osd, new FileInputStream(tempOutputFile));
        osd.setContentHash(metadata.getContentHash());
        osd.setContentPath(metadata.getContentPath());
        osd.setContentSize(metadata.getContentSize());
        osd.setFormatId(format.getId());
        convertContentToTikaMetaset(osd, new FileInputStream(tempOutputFile), format);
        deleteTempFile(tempOutputFile);
    }

    private void convertContentToTikaMetaset(ObjectSystemData osd, InputStream contentStream, Format format) throws IOException {
        OsdMetaDao osdMetaDao = new OsdMetaDao();
        if (tikaService.isEnabled() && tikaMetasetTypeId != null && format.getIndexMode() == IndexMode.TIKA) {
            String         tikaMetadata = parseWithTika(contentStream, format);
            log.debug("Tika returned: "+tikaMetadata);
            Optional<Meta> tikaMetaset  = osdMetaDao.listByOsd(osd.getId()).stream().filter(meta -> meta.getTypeId().equals(tikaMetasetTypeId)).findFirst();
            if (tikaMetaset.isPresent()) {
                Meta tikaMeta = tikaMetaset.get();
                tikaMeta.setContent(tikaMetadata);
                try {
                    osdMetaDao.update(List.of(tikaMeta));
                } catch (SQLException e) {
                    throw new CinnamonException("Failed to update tika metaset:", e);
                }
            } else {
                Meta tikaMeta = new Meta(osd.getId(), tikaMetasetTypeId, tikaMetadata);
                List<Meta> metas = osdMetaDao.create(List.of(tikaMeta));
                log.debug("tikaMeta: "+metas.get(0));
            }
        }
    }

    private String parseWithTika(InputStream contentStream, Format format) throws IOException {
        File tempData = File.createTempFile("tika-indexing-", ".data");
        try (FileOutputStream tempFos = new FileOutputStream(tempData)) {
            tempFos.write(contentStream.readAllBytes());
            tempFos.flush();
            return tikaService.parseData(tempData, format);
        } finally {
            FileUtils.delete(tempData);
        }
    }

    private void setSummary(HttpServletRequest request, CinnamonResponse response, UserAccount user, OsdDao osdDao) throws
            IOException {
        SetSummaryRequest summaryRequest = xmlMapper.readValue(request.getInputStream(), SetSummaryRequest.class);
        ObjectSystemData  osd            = osdDao.getObjectById(summaryRequest.getId()).orElseThrow(ErrorCode.OBJECT_NOT_FOUND.getException());
        authorizationService.throwUpUnlessUserOrOwnerHasPermission(osd, DefaultPermission.SET_SUMMARY, user,
                ErrorCode.NO_SET_SUMMARY_PERMISSION);
        osd.setSummary(summaryRequest.getSummary());
        osdDao.updateOsd(osd, true);
        response.responseIsGenericOkay();
    }

    private void getSummaries(HttpServletRequest request, CinnamonResponse response, UserAccount user, OsdDao
            osdDao) throws IOException {
        IdListRequest          idListRequest = xmlMapper.readValue(request.getInputStream(), IdListRequest.class);
        SummaryWrapper         wrapper       = new SummaryWrapper();
        List<ObjectSystemData> osds          = osdDao.getObjectsById(idListRequest.getIdList(), true);
        osds.forEach(osd -> {
            if (authorizationService.hasUserOrOwnerPermission(osd, DefaultPermission.READ_OBJECT_SYS_METADATA, user)) {
                wrapper.getSummaries().add(new Summary(osd.getId(), osd.getSummary()));
            }
        });
        response.setWrapper(wrapper);
    }

    private void update(HttpServletRequest request, CinnamonResponse response, UserAccount user, OsdDao
            osdDao) throws IOException {
        UpdateOsdRequest updateRequest = xmlMapper.readValue(request.getInputStream(), UpdateOsdRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());

        ObjectSystemData osd = osdDao.getObjectById(updateRequest.getId())
                .orElseThrow(ErrorCode.OBJECT_NOT_FOUND.getException());
        if (osd.lockedByOtherUser(user)) {
            ErrorCode.OBJECT_LOCKED_BY_OTHER_USER.throwUp();
            return;
        }
        if (!osd.lockedByUser(user)) {
            ErrorCode.OBJECT_MUST_BE_LOCKED_BY_USER.throwUp();
            return;
        }

        AccessFilter accessFilter = AccessFilter.getInstance(user);
        FolderDao    folderDao    = new FolderDao();

        boolean changed = false;
        // change parent folder
        Long parentId = updateRequest.getParentFolderId();
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
        String name = updateRequest.getName();
        if (name != null) {
            if (!accessFilter.hasPermissionOnOwnable(osd, DefaultPermission.SET_NAME, osd)) {
                throw ErrorCode.NO_NAME_WRITE_PERMISSION.exception();
            }
            osd.setName(name);
            changed = true;
        }

        // change type
        Long typeId = updateRequest.getObjectTypeId();
        if (typeId != null) {
            if (!accessFilter.hasPermissionOnOwnable(osd, DefaultPermission.SET_TYPE, osd)) {
                throw NO_TYPE_WRITE_PERMISSION.exception();
            }
            ObjectType type = new ObjectTypeDao().getObjectTypeById(typeId)
                    .orElseThrow(ErrorCode.OBJECT_TYPE_NOT_FOUND.getException());
            osd.setTypeId(type.getId());
            changed = true;
        }

        // change acl
        Long aclId = updateRequest.getAclId();
        if (aclId != null) {
            if (!accessFilter.hasPermissionOnOwnable(osd, DefaultPermission.SET_ACL, osd)) {
                ErrorCode.MISSING_SET_ACL_PERMISSION.throwUp();
            }
            Acl acl = new AclDao().getAclById(aclId)
                    .orElseThrow(ErrorCode.ACL_NOT_FOUND.getException());
            osd.setAclId(acl.getId());
            changed = true;
        }

        // change owner
        Long ownerId = updateRequest.getOwnerId();
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
        Long languageId = updateRequest.getLanguageId();
        if (languageId != null) {
            if (!accessFilter.hasPermissionOnOwnable(osd, DefaultPermission.SET_LANGUAGE, osd)) {
                NO_UPDATE_LANGUAGE_PERMISSION.throwUp();
            }
            Language language = new LanguageDao().getLanguageById(languageId)
                    .orElseThrow(ErrorCode.LANGUAGE_NOT_FOUND.getException());
            osd.setLanguageId(language.getId());
            changed = true;
        }

        // update osd:
        if (changed) {
            osdDao.updateOsd(osd, true);
        }

        response.responseIsGenericOkay();
    }

    private void getObjectsById(HttpServletRequest request, CinnamonResponse response, UserAccount user, OsdDao
            osdDao) throws IOException {
        OsdRequest             osdRequest   = xmlMapper.readValue(request.getInputStream(), OsdRequest.class);
        List<ObjectSystemData> osds         = osdDao.getObjectsById(osdRequest.getIds(), osdRequest.isIncludeSummary());
        List<ObjectSystemData> filteredOsds = authorizationService.filterObjectsByBrowsePermission(osds, user);

        if (osdRequest.isIncludeCustomMetadata()) {
            OsdMetaDao metaDao = new OsdMetaDao();
            boolean hasReadMetaPermission = filteredOsds.stream().allMatch(osd ->
                    authorizationService.hasUserOrOwnerPermission(osd, DefaultPermission.READ_OBJECT_CUSTOM_METADATA, user)
            );
            if (!hasReadMetaPermission) {
                ErrorCode.NO_READ_CUSTOM_METADATA_PERMISSION.throwUp();
                return;
            }
            filteredOsds.forEach(osd -> osd.setMetas(metaDao.listByOsd(osd.getId())));
        }

        OsdWrapper wrapper = new OsdWrapper();
        wrapper.setOsds(filteredOsds);
        response.setWrapper(wrapper);
    }

    private void getObjectsByFolderId(HttpServletRequest request, CinnamonResponse response, UserAccount
            user, OsdDao osdDao) throws IOException {
        OsdByFolderRequest     osdRequest     = xmlMapper.readValue(request.getInputStream(), OsdByFolderRequest.class);
        long                   folderId       = osdRequest.getFolderId();
        boolean                includeSummary = osdRequest.isIncludeSummary();
        boolean                includeMeta    = osdRequest.isIncludeCustomMetadata();
        List<ObjectSystemData> osds           = osdDao.getObjectsByFolderId(folderId, includeSummary, osdRequest.getVersionPredicate());
        List<ObjectSystemData> filteredOsds   = authorizationService.filterObjectsByBrowsePermission(osds, user);
        OsdMetaDao             metaDao        = new OsdMetaDao();
        if (includeMeta) {
            filteredOsds.forEach(osd -> osd.setMetas(metaDao.listByOsd(osd.getId())));
        }
        LinkDao    linkDao       = new LinkDao();
        List<Link> links         = linkDao.getLinksByFolderId(folderId);
        List<Link> filteredLinks = authorizationService.filterLinksByBrowsePermission(links, user);

        OsdWrapper wrapper = new OsdWrapper();
        wrapper.setOsds(filteredOsds);
        wrapper.setLinks(filteredLinks);
        if (osdRequest.isLinksAsOsd()) {
            List<ObjectSystemData> references = osdDao.getObjectsById(filteredLinks.stream().map(Link::getObjectId).filter(Objects::nonNull).collect(Collectors.toList()), includeSummary);
            if (includeMeta) {
                references.forEach(osd -> osd.setMetas(metaDao.listByOsd(osd.getId())));
            }
            wrapper.setReferences(references);
        }

        response.setWrapper(wrapper);
    }

    private void newVersion(HttpServletRequest request, CinnamonResponse response, UserAccount user, OsdDao osdDao) throws
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
        osdDao.updateOsd(preOsd, false);

        if (osd.getCmnVersion().matches("^\\d+$")) {
            osd.setLatestHead(true);
        }
        // save here to generate Id so metasets can be created and linked to the OSD.
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
                            MetasetType metasetType = determineMetasetType(metadata.getTypeId(), metadata.getTypeName());
                            Meta        meta        = new Meta(osd.getId(), metasetType.getId(), metadata.getContent());
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
        }

        // saveFileUpload
        Part file = request.getPart("file");
        if (file != null) {
            storeFileUpload(file.getInputStream(), osd, versionRequest.getFormatId());
        }

        // set lifecycle state
        if (preOsd.getLifecycleStateId() != null) {
            LifecycleState lifecycleState = new LifecycleStateDao()
                    .getLifecycleStateById(preOsd.getLifecycleStateId())
                    .orElseThrow(ErrorCode.LIFECYCLE_STATE_NOT_FOUND.getException());
            State             stateImpl         = StateProviderService.getInstance().getStateProvider(lifecycleState.getStateClass()).getState();
            StateChangeResult attachStateResult = stateImpl.enter(osd, lifecycleState.getLifecycleStateConfig());
            if (!attachStateResult.isSuccessful()) {
                throw ErrorCode.LIFECYCLE_STATE_CHANGE_FAILED.exception();
            }
            osd.setLifecycleStateId(lifecycleState.getLifecycleStateForCopyId());
        }
        osdDao.updateOsd(osd, false);
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
        tikaService = ((TikaService) getServletContext().getAttribute(TIKA_SERVICE));
    }
}
