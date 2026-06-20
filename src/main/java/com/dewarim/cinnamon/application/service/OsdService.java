package com.dewarim.cinnamon.application.service;

import com.dewarim.cinnamon.DefaultPermission;
import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.Constants;
import com.dewarim.cinnamon.api.content.ContentMetadata;
import com.dewarim.cinnamon.api.content.ContentProvider;
import com.dewarim.cinnamon.dao.*;
import com.dewarim.cinnamon.model.*;
import com.dewarim.cinnamon.model.relations.Relation;
import com.dewarim.cinnamon.provider.ContentProviderService;
import com.dewarim.cinnamon.security.authorization.AccessFilter;
import com.dewarim.cinnamon.security.authorization.AuthorizationService;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class OsdService {

    private final ContentProviderService contentProviderService;
    private final DeleteOsdService       deleteOsdService     = new DeleteOsdService();
    private final AuthorizationService   authorizationService = new AuthorizationService();

    public OsdService(ContentProviderService contentProviderService) {
        this.contentProviderService = contentProviderService;
    }

    public record ContentResult(InputStream stream, Format format, ObjectSystemData osd) {}

    public ObjectSystemData getOsd(Long id, UserAccount user) {
        ObjectSystemData osd = new OsdDao().getObjectById(id).orElseThrow(ErrorCode.OBJECT_NOT_FOUND::exception);
        AccessFilter filter = AccessFilter.getInstance(user);
        if (!filter.hasBrowsePermissionForOwnable(osd)) {
            throw ErrorCode.NO_BROWSE_PERMISSION.exception();
        }
        return osd;
    }

    public ObjectSystemData createOsd(String name, Long parentId, Long aclId, Long typeId,
                                      Long formatId, Long lifecycleStateId, String summary,
                                      InputStream content, UserAccount user) throws IOException {
        FolderDao folderDao    = new FolderDao();
        Folder    parentFolder = folderDao.getFolderById(parentId).orElseThrow(ErrorCode.PARENT_FOLDER_NOT_FOUND::exception);

        if (!authorizationService.userHasPermission(parentFolder.getAclId(), DefaultPermission.CREATE_OBJECT, user)) {
            throw ErrorCode.NO_CREATE_PERMISSION.exception();
        }

        ObjectSystemData osd = new ObjectSystemData();
        osd.setParentId(parentFolder.getId());
        osd.setName(name);
        osd.setLatestHead(true);

        osd.setAclId(new AclDao().getObjectById(aclId).orElseThrow(ErrorCode.ACL_NOT_FOUND::exception).getId());
        osd.setOwnerId(user.getId());

        osd.setTypeId(new ObjectTypeDao().getObjectById(typeId).orElseThrow(ErrorCode.OBJECT_TYPE_NOT_FOUND::exception).getId());

        if (lifecycleStateId != null) {
            osd.setLifecycleStateId(new LifecycleStateDao().getLifecycleStateById(lifecycleStateId)
                    .orElseThrow(ErrorCode.LIFECYCLE_STATE_NOT_FOUND::exception).getId());
        }

        osd.setLanguageId(new LanguageDao().getLanguageByIsoCode(Constants.LANGUAGE_UNDETERMINED_ISO_CODE)
                .orElseThrow(ErrorCode.DB_IS_MISSING_LANGUAGE_CODE::exception).getId());

        osd.setCreatorId(user.getId());
        osd.setModifierId(user.getId());
        osd.setSummary(Objects.requireNonNullElse(summary, Constants.DEFAULT_SUMMARY));

        if (user.isChangeTracking()) {
            osd.setMetadataChanged(true);
        }

        OsdDao osdDao = new OsdDao();
        osd = osdDao.saveOsd(osd);

        if (content != null && formatId != null) {
            storeContent(osd, content, formatId);
            if (user.isChangeTracking()) {
                osd.setContentChanged(true);
            }
            osdDao.updateOsd(osd, false);
        }
        return osd;
    }

    public void deleteOsds(List<Long> ids, boolean deleteDescendants, boolean deleteAllVersions, UserAccount user) {
        OsdDao                 osdDao = new OsdDao();
        List<ObjectSystemData> osds   = osdDao.getObjectsById(ids, false);
        osds.sort(Comparator.comparingLong(ObjectSystemData::getId).reversed());
        deleteOsdService.verifyAndDelete(osds, deleteDescendants, deleteAllVersions, user);
    }

    public void deleteOsd(Long id, UserAccount user) {
        deleteOsds(List.of(id), false, false, user);
    }

    public ContentResult getContent(Long id, UserAccount user) throws IOException {
        OsdDao           osdDao = new OsdDao();
        ObjectSystemData osd    = osdDao.getObjectById(id).orElseThrow(ErrorCode.OBJECT_NOT_FOUND::exception);
        authorizationService.throwUpUnlessUserOrOwnerHasPermission(osd, DefaultPermission.READ_OBJECT_CONTENT, user,
                ErrorCode.NO_READ_PERMISSION);
        if (osd.getContentSize() == null || osd.getContentSize() == 0) {
            throw ErrorCode.OBJECT_HAS_NO_CONTENT.exception();
        }
        Format          format   = new FormatDao().getObjectById(osd.getFormatId()).orElseThrow();
        ContentProvider provider = contentProviderService.getContentProvider(osd.getContentProvider());
        InputStream     stream   = provider.getContentStream(osd);
        return new ContentResult(stream, format, osd);
    }

    public void updateOsd(Long osdId, String name, Long parentId, Long aclId, Long ownerId, Long typeId,
                          UserAccount user) {
        OsdDao           osdDao = new OsdDao();
        ObjectSystemData osd    = osdDao.getObjectById(osdId).orElseThrow(ErrorCode.OBJECT_NOT_FOUND::exception);

        if (osd.lockedByOtherUser(user) && !authorizationService.currentUserIsSuperuser()) {
            throw ErrorCode.OBJECT_LOCKED_BY_OTHER_USER.exception();
        }

        AccessFilter filter  = AccessFilter.getInstance(user);
        boolean      changed = false;

        if (name != null && !name.isBlank() && !name.equals(osd.getName())) {
            if (!filter.hasPermissionOnOwnable(osd, DefaultPermission.SET_NAME, osd)) {
                throw ErrorCode.NO_NAME_WRITE_PERMISSION.exception();
            }
            osd.setName(name.strip());
            changed = true;
        }

        if (parentId != null && !parentId.equals(osd.getParentId())) {
            Folder newParent = new FolderDao().getFolderById(parentId).orElseThrow(ErrorCode.PARENT_FOLDER_NOT_FOUND::exception);
            if (!filter.hasPermissionOnOwnable(newParent, DefaultPermission.CREATE_OBJECT, newParent)) {
                throw ErrorCode.NO_CREATE_PERMISSION.exception();
            }
            if (!filter.hasPermissionOnOwnable(osd, DefaultPermission.SET_PARENT, osd)) {
                throw ErrorCode.NO_SET_PARENT_PERMISSION.exception();
            }
            osd.setParentId(newParent.getId());
            changed = true;
        }

        if (aclId != null && !aclId.equals(osd.getAclId())) {
            if (!filter.hasPermissionOnOwnable(osd, DefaultPermission.SET_ACL, osd)) {
                throw ErrorCode.MISSING_SET_ACL_PERMISSION.exception();
            }
            osd.setAclId(new AclDao().getObjectById(aclId).orElseThrow(ErrorCode.ACL_NOT_FOUND::exception).getId());
            changed = true;
        }

        if (ownerId != null && !ownerId.equals(osd.getOwnerId())) {
            if (!filter.hasPermissionOnOwnable(osd, DefaultPermission.SET_OWNER, osd)) {
                throw ErrorCode.NO_SET_OWNER_PERMISSION.exception();
            }
            osd.setOwnerId(new UserAccountDao().getUserAccountById(ownerId).orElseThrow(ErrorCode.USER_ACCOUNT_NOT_FOUND::exception).getId());
            changed = true;
        }

        if (typeId != null && !typeId.equals(osd.getTypeId())) {
            if (!filter.hasPermissionOnOwnable(osd, DefaultPermission.SET_TYPE, osd)) {
                throw ErrorCode.NO_TYPE_WRITE_PERMISSION.exception();
            }
            osd.setTypeId(new ObjectTypeDao().getObjectById(typeId).orElseThrow(ErrorCode.OBJECT_TYPE_NOT_FOUND::exception).getId());
            changed = true;
        }

        if (changed) {
            if (user.isChangeTracking()) {
                osd.setMetadataChanged(true);
            }
            osdDao.updateOsd(osd, true);
        }
    }

    public ObjectSystemData newVersion(Long osdId, UserAccount user) {
        OsdDao           osdDao = new OsdDao();
        ObjectSystemData preOsd = osdDao.getObjectById(osdId).orElseThrow(ErrorCode.OBJECT_NOT_FOUND::exception);
        FolderDao        folderDao = new FolderDao();
        Folder           folder = folderDao.getFolderById(preOsd.getParentId()).orElseThrow(ErrorCode.FOLDER_NOT_FOUND::exception);

        authorizationService.throwUpUnlessUserOrOwnerHasPermission(folder, DefaultPermission.CREATE_OBJECT, user,
                ErrorCode.NO_CREATE_PERMISSION);
        authorizationService.throwUpUnlessUserOrOwnerHasPermission(preOsd, DefaultPermission.VERSION_OBJECT, user,
                ErrorCode.NO_VERSION_PERMISSION);

        Optional<String> lastVersion = osdDao.findLastDescendantVersion(preOsd.getId());
        ObjectSystemData newOsd      = preOsd.createNewVersion(user, lastVersion.orElse(null));

        preOsd.setLatestBranch(false);
        preOsd.setLatestHead(false);
        osdDao.updateOsd(preOsd, false);

        if (newOsd.getCmnVersion().matches("^\\d+$")) {
            newOsd.setLatestHead(true);
        }
        return osdDao.saveOsd(newOsd);
    }

    public ObjectSystemData copyOsd(Long osdId, Long targetFolderId, UserAccount user) throws IOException {
        OsdDao           osdDao = new OsdDao();
        ObjectSystemData source = osdDao.getObjectById(osdId).orElseThrow(ErrorCode.OBJECT_NOT_FOUND::exception);
        AccessFilter     filter = AccessFilter.getInstance(user);

        if (!filter.hasPermissionOnOwnable(source, DefaultPermission.BROWSE, source)) {
            throw ErrorCode.NO_BROWSE_PERMISSION.exception();
        }
        if (!filter.hasPermissionOnOwnable(source, DefaultPermission.READ_OBJECT_CONTENT, source)) {
            throw ErrorCode.NO_READ_PERMISSION.exception();
        }

        Folder targetFolder = new FolderDao().getFolderById(targetFolderId).orElseThrow(ErrorCode.FOLDER_NOT_FOUND::exception);
        if (!filter.hasPermissionOnOwnable(targetFolder, DefaultPermission.CREATE_OBJECT, targetFolder)) {
            throw ErrorCode.NO_CREATE_PERMISSION.exception();
        }

        ObjectSystemData copy = new ObjectSystemData();
        copy.setAclId(targetFolder.getAclId());
        copy.setParentId(targetFolder.getId());
        copy.setName("Copy_" + source.getName());
        copy.setOwnerId(user.getId());
        copy.setCmnVersion("1");
        copy.setLatestBranch(true);
        copy.setLatestHead(true);
        copy.setModifierId(user.getId());
        copy.setModified(LocalDateTime.now());
        copy.setCreatorId(user.getId());
        copy.setCreated(LocalDateTime.now());
        copy.setTypeId(source.getTypeId());
        copy.setLanguageId(source.getLanguageId());
        copy.setSummary(source.getSummary());
        osdDao.saveOsd(copy);

        if (source.getContentHash() != null) {
            ContentProvider provider = contentProviderService.getContentProvider(source.getContentProvider());
            try (InputStream stream = provider.getContentStream(source)) {
                ContentMetadata metadata = provider.writeContentStream(copy, stream);
                copy.setContentHash(metadata.getContentHash());
                copy.setContentPath(metadata.getContentPath());
                copy.setContentSize(metadata.getContentSize());
                copy.setContentProvider(provider.getName());
                copy.setFormatId(source.getFormatId());
                osdDao.updateOsd(copy, false);
            }
        }
        return copy;
    }

    public List<Meta> getMeta(Long osdId, List<Long> typeIds, UserAccount user) {
        ObjectSystemData osd    = new OsdDao().getObjectById(osdId).orElseThrow(ErrorCode.OBJECT_NOT_FOUND::exception);
        AccessFilter     filter = AccessFilter.getInstance(user);
        if (!filter.hasPermissionOnOwnable(osd, DefaultPermission.READ_OBJECT_CUSTOM_METADATA, osd)) {
            throw ErrorCode.NO_READ_CUSTOM_METADATA_PERMISSION.exception();
        }
        OsdMetaDao metaDao = new OsdMetaDao();
        return typeIds != null ? metaDao.getMetaByTypeIdsAndOsd(typeIds, osdId) : metaDao.listByOsd(osdId);
    }

    public List<Meta> getMeta(Long osdId, UserAccount user) {
        return getMeta(osdId, null, user);
    }

    public List<Relation> getRelations(Long osdId, UserAccount user) {
        ObjectSystemData osd    = new OsdDao().getObjectById(osdId).orElseThrow(ErrorCode.OBJECT_NOT_FOUND::exception);
        AccessFilter     filter = AccessFilter.getInstance(user);
        if (!filter.hasPermissionOnOwnable(osd, DefaultPermission.BROWSE, osd)) {
            throw ErrorCode.NO_BROWSE_PERMISSION.exception();
        }
        return new RelationDao().getRelationsOrMode(List.of(osdId), List.of(osdId), null, false);
    }

    private void storeContent(ObjectSystemData osd, InputStream content, Long formatId) throws IOException {
        Format format = new FormatDao().getObjectById(formatId).orElseThrow(ErrorCode.FORMAT_NOT_FOUND::exception);
        Path   tmp    = Files.createTempFile("cinnamon-ui-upload-", ".data");
        Files.copy(content, tmp, StandardCopyOption.REPLACE_EXISTING);

        ContentProvider provider = contentProviderService.getContentProvider(osd.getContentProvider());
        ContentMetadata metadata = provider.writeContentStream(osd, new FileInputStream(tmp.toFile()));
        osd.setContentHash(metadata.getContentHash());
        osd.setContentPath(metadata.getContentPath());
        osd.setContentSize(metadata.getContentSize());
        osd.setFormatId(format.getId());
        Files.deleteIfExists(tmp);
    }
}
