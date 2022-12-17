package com.dewarim.cinnamon.application.service;

import com.dewarim.cinnamon.DefaultPermission;
import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.Ownable;
import com.dewarim.cinnamon.dao.CrudDao;
import com.dewarim.cinnamon.dao.MetaDao;
import com.dewarim.cinnamon.dao.MetasetTypeDao;
import com.dewarim.cinnamon.model.Meta;
import com.dewarim.cinnamon.model.MetasetType;
import com.dewarim.cinnamon.model.UserAccount;
import com.dewarim.cinnamon.security.authorization.AuthorizationService;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MetaService<T extends CrudDao<Meta> & MetaDao, O extends CrudDao<? extends Ownable>> {

    private final AuthorizationService authorizationService = new AuthorizationService();
    void throwUnlessCustomMetaIsWritable(Ownable ownable, UserAccount user) {
        boolean readAllowed = authorizationService.hasUserOrOwnerPermission(ownable, DefaultPermission.READ_OBJECT_CUSTOM_METADATA, user);
        if (!readAllowed) {
            throw ErrorCode.NO_WRITE_CUSTOM_METADATA_PERMISSION.getException().get();
        }
    }

    public void deleteMeta(T dao, List<Long> ids, O ownableDao, UserAccount user){
        List<Meta> metas = dao.getObjectsById(ids);
        if (metas.isEmpty() || metas.size() != ids.size()) {
            throw ErrorCode.METASET_NOT_FOUND.exception();
        }
        List<? extends Ownable> ownables = ownableDao.getObjectsById(metas.stream().map(Meta::getObjectId).collect(Collectors.toList()));
        ownables.forEach(ownable -> throwUnlessCustomMetaIsWritable(ownable, user));
        dao.delete(ids);
    }

    public List<Meta> createMeta(T dao, List<Meta> metas, O ownableDao, UserAccount user){
        // load objects
        List<Long> ownableIds = metas.stream().map(Meta::getObjectId).collect(Collectors.toList());
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
        return dao.create(metasToCreate);
    }

}
