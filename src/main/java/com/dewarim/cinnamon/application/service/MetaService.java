package com.dewarim.cinnamon.application.service;

import com.dewarim.cinnamon.DefaultPermission;
import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.FailedRequestException;
import com.dewarim.cinnamon.api.Ownable;
import com.dewarim.cinnamon.dao.*;
import com.dewarim.cinnamon.model.Meta;
import com.dewarim.cinnamon.model.MetasetType;
import com.dewarim.cinnamon.model.UserAccount;
import com.dewarim.cinnamon.model.index.IndexJob;
import com.dewarim.cinnamon.model.index.IndexJobAction;
import com.dewarim.cinnamon.model.index.IndexJobType;
import com.dewarim.cinnamon.security.authorization.AuthorizationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.dewarim.cinnamon.ErrorCode.OBJECT_NOT_FOUND;

public class MetaService<T extends CrudDao<Meta> & MetaDao, O extends CrudDao<? extends Ownable>> {
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
        List<Long>              objectIds = metas.stream().map(Meta::getObjectId).toList();
        List<? extends Ownable> ownables  = ownableDao.getObjectsById(objectIds);
        for (Ownable ownable : ownables) {
            throwUnlessCustomMetaIsWritable(ownable, user);
        }
        List<Long> metaIds =metas.stream().map(Meta::getId).toList(); 
        metaDao.delete(metaIds);
        updateIndex(metas, ownableDao);
    }

    // this may create additional indexItems if the OSD or Folder is subject to update anyway.
    private void updateIndex(List<Meta> metas, O ownableDao) {
        IndexJobDao indexJobDao = new IndexJobDao();
        metas.forEach(meta -> {
            IndexJob indexJob;
            // a little bit hacky
            if(ownableDao instanceof OsdDao){
                indexJob = new IndexJob(IndexJobType.OSD, meta.getObjectId(), IndexJobAction.UPDATE, false);    
            }
            else{
                indexJob = new IndexJob(IndexJobType.FOLDER, meta.getObjectId(), IndexJobAction.UPDATE, false);
            }
            indexJobDao.insertIndexJob(indexJob);
            log.debug("insert index job: "+indexJob);
        });
    }

    public List<Meta> createMeta(T dao, List<Meta> metas, O ownableDao, UserAccount user) {
        // load objects
        List<Long>              ownableIds = metas.stream().map(Meta::getObjectId).collect(Collectors.toList());
        List<? extends Ownable> ownables   = ownableDao.getObjectsById(ownableIds);
        if (ownables.size() != ownableIds.size()) {
            throw ErrorCode.OBJECT_NOT_FOUND.getException().get();
        }

        Map<Long, Ownable> ownableMap = ownables.stream().filter(osd -> {
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
        updateIndex(metas, ownableDao);
        return dao.create(metasToCreate);
    }

    public void updateMeta(T dao, List<Meta> metas, O ownableDao, UserAccount user) {

        List<Meta> updates = new ArrayList<>();
        for (Meta metaUpdate : metas) {
            Meta meta = dao.getMetaById(metaUpdate.getId())
                    .orElseThrow(ErrorCode.METASET_NOT_FOUND.getException());
            Ownable ownable = ownableDao.getObjectById(meta.getObjectId())
                    .orElseThrow(OBJECT_NOT_FOUND.getException());
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

        try{
            dao.update(updates);
            updateIndex(metas, ownableDao);
        }
        catch (SQLException e){
            log.warn(String.format("DB update failed: %s with status %s and error code %d",
                    e.getMessage(), e.getSQLState(), e.getErrorCode()),e);
            throw new FailedRequestException(ErrorCode.DB_UPDATE_FAILED, e);
        }
    }
}
