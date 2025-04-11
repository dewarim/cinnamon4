package com.dewarim.cinnamon.application.service;

import com.dewarim.cinnamon.DefaultPermission;
import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.FailedRequestException;
import com.dewarim.cinnamon.api.Ownable;
import com.dewarim.cinnamon.api.OwnableWithMetadata;
import com.dewarim.cinnamon.application.exception.CinnamonException;
import com.dewarim.cinnamon.dao.*;
import com.dewarim.cinnamon.model.*;
import com.dewarim.cinnamon.model.index.IndexJob;
import com.dewarim.cinnamon.model.index.IndexJobAction;
import com.dewarim.cinnamon.model.index.IndexJobType;
import com.dewarim.cinnamon.security.authorization.AuthorizationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.dewarim.cinnamon.ErrorCode.OBJECT_NOT_FOUND;

public class MetaService<T extends CrudDao<Meta> & MetaDao, O extends CrudDao<? extends OwnableWithMetadata>> {
    private static final Logger log = LogManager.getLogger(MetaService.class);

    private final AuthorizationService authorizationService = new AuthorizationService();

    void throwUnlessCustomMetaIsWritable(Ownable ownable, UserAccount user) {
        boolean readAllowed = authorizationService.hasUserOrOwnerPermission(ownable, DefaultPermission.WRITE_OBJECT_CUSTOM_METADATA, user);
        if (!readAllowed) {
            throw ErrorCode.NO_WRITE_CUSTOM_METADATA_PERMISSION.getException().get();
        }
    }

    public void deleteMetas(T metaDao, List<Meta> metas, O ownableDao, UserAccount user) {
        if (metas.isEmpty()) {
            return;
        }
        List<Long>                          objectIds = metas.stream().map(Meta::getObjectId).toList();
        List<? extends OwnableWithMetadata> ownables  = ownableDao.getObjectsById(objectIds);
        for (Ownable ownable : ownables) {
            throwUnlessCustomMetaIsWritable(ownable, user);
        }
        List<Long> metaIds = metas.stream().map(Meta::getId).toList();
        metaDao.delete(metaIds);
        updateMetadataChanged(user, ownables, ownableDao);
        updateIndex(metas, ownableDao);
    }

    // this may create additional indexItems if the OSD or Folder is subject to update anyway.
    private void updateIndex(List<Meta> metas, O ownableDao) {
        IndexJobDao indexJobDao = new IndexJobDao();
        metas.forEach(meta -> {
            IndexJob indexJob;
            // a little bit hacky
            if (ownableDao instanceof OsdDao) {
                indexJob = new IndexJob(IndexJobType.OSD, meta.getObjectId(), IndexJobAction.UPDATE, false);
            }
            else {
                indexJob = new IndexJob(IndexJobType.FOLDER, meta.getObjectId(), IndexJobAction.UPDATE, false);
            }
            indexJobDao.insertIndexJob(indexJob);
            log.debug("insert index job: {}", indexJob);
        });
    }

    public List<Meta> createMeta(T dao, List<Meta> metas, O ownableDao, UserAccount user) {
        if (metas.isEmpty()) {
            return List.of();
        }
        // load objects
        Set<Long>                           ownableIds = metas.stream().map(Meta::getObjectId).collect(Collectors.toSet());
        List<? extends OwnableWithMetadata> ownables   = ownableDao.getObjectsById(ownableIds);
        if (ownables.size() != ownableIds.size()) {
            throw new FailedRequestException(ErrorCode.OBJECT_NOT_FOUND, "Could not find one of the following OSDs: " +
                    (ownableIds.stream().map(Object::toString).collect(Collectors.joining(","))));
        }

        Map<Long, OwnableWithMetadata> ownableMap = ownables.stream().filter(osd -> {
            throwUnlessCustomMetaIsWritable(osd, user);
            return true;
        }).collect(Collectors.toMap(Ownable::getId, Function.identity()));

        // load metasetTypes
        MetasetTypeDao         metasetTypeDao = new MetasetTypeDao();
        Map<Long, MetasetType> metasetTypes   = metasetTypeDao.list().stream().collect(Collectors.toMap(MetasetType::getId, Function.identity()));
        // check that request does not contain unknown metasetTypeIds
        Set<Long> requestedTypeIds = metas.stream().map(Meta::getTypeId).collect(Collectors.toUnmodifiableSet());
        if (!requestedTypeIds.stream().allMatch(metasetTypes::containsKey)) {
            throw ErrorCode.METASET_TYPE_NOT_FOUND.exception();
        }

        // does meta already exist and is unique?
        ownableMap.keySet().forEach(id -> {
            List<Long> uniqueMetaTypeIds = dao.getUniqueMetaTypeIdsOfObject(id);
            if (requestedTypeIds.stream().anyMatch(uniqueMetaTypeIds::contains)) {
                throw ErrorCode.METASET_IS_UNIQUE_AND_ALREADY_EXISTS.exception();
            }
        });

        List<Meta> metasToCreate = metas.stream().map(meta -> new Meta(meta.getObjectId(), meta.getTypeId(), meta.getContent()))
                .collect(Collectors.toList());
        updateMetadataChanged(user, ownableMap.values().stream().toList(), ownableDao);
        updateIndex(metas, ownableDao);
        return dao.create(metasToCreate);
    }

    public void updateMeta(T dao, List<Meta> metas, O ownableDao, UserAccount user) {

        List<Meta>                updates  = new ArrayList<>();
        List<OwnableWithMetadata> ownables = new ArrayList<>();
        for (Meta metaUpdate : metas) {
            Meta meta = dao.getMetaById(metaUpdate.getId())
                    .orElseThrow(ErrorCode.METASET_NOT_FOUND.getException());
            OwnableWithMetadata ownable = ownableDao.getObjectById(meta.getObjectId())
                    .orElseThrow(() -> new FailedRequestException(OBJECT_NOT_FOUND, "Could not find Ownable with id:"+ String.valueOf(meta.getObjectId())));
            ownables.add(ownable);
            if (!ownable.getId().equals(metaUpdate.getObjectId()) ||
                    !meta.getTypeId().equals(metaUpdate.getTypeId())) {
                // changing the type or moving a metaset to another owning object has no use case yet,
                // so let's not do that until we require it.
                throw ErrorCode.INVALID_UPDATE.exception();
            }
            throwUnlessCustomMetaIsWritable(ownable, user);
            meta.setContent(metaUpdate.getContent());
            updates.add(metaUpdate);
        }

        try {
            dao.update(updates);
            updateMetadataChanged(user, ownables, ownableDao);
            updateIndex(metas, ownableDao);
        } catch (SQLException e) {
            log.warn(String.format("DB update failed: %s with status %s and error code %d",
                    e.getMessage(), e.getSQLState(), e.getErrorCode()), e);
            throw new FailedRequestException(ErrorCode.DB_UPDATE_FAILED, e);
        }
    }

    /**
     * Update metadataChanged
     */
    private void updateMetadataChanged(UserAccount user, List<? extends OwnableWithMetadata> ownables, O dao) {
        if (user.isChangeTracking()) {
            for (OwnableWithMetadata item : ownables) {
                item.setMetadataChanged(true);
                item.setModified(new Date());
                item.setModifierId(user.getId());
            }
            try {
                if (dao.getTypeClassName().equals(ObjectSystemData.class.getName())) {
                    List<ObjectSystemData> osds = (List<ObjectSystemData>) ownables;
                    ((OsdDao) dao).update(osds);
                }
                else {
                    List<Folder> folders = (List<Folder>) ownables;
                    ((FolderDao) dao).update(folders);
                }
            } catch (SQLException e) {
                throw new CinnamonException("Failed to update metadataChanged flag", e);
            }
        }
    }

}
