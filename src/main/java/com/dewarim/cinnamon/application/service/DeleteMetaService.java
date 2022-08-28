package com.dewarim.cinnamon.application.service;

import com.dewarim.cinnamon.DefaultPermission;
import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.FailedRequestException;
import com.dewarim.cinnamon.api.Ownable;
import com.dewarim.cinnamon.dao.CrudDao;
import com.dewarim.cinnamon.model.Meta;
import com.dewarim.cinnamon.model.UserAccount;
import com.dewarim.cinnamon.security.authorization.AuthorizationService;

import java.util.List;
import java.util.stream.Collectors;

public class DeleteMetaService<T extends CrudDao<Meta>, O extends CrudDao<? extends Ownable>> {

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
            throw new FailedRequestException(ErrorCode.METASET_NOT_FOUND);
        }
        List<? extends Ownable> ownables = ownableDao.getObjectsById(metas.stream().map(Meta::getObjectId).collect(Collectors.toList()));
        ownables.forEach(ownable -> throwUnlessCustomMetaIsWritable(ownable, user));
        dao.delete(ids);
    }

}
