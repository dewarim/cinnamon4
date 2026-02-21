package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.DefaultPermission;
import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.Ownable;
import com.dewarim.cinnamon.application.CinnamonResponse;
import com.dewarim.cinnamon.application.RequestScope;
import com.dewarim.cinnamon.dao.MetasetTypeDao;
import com.dewarim.cinnamon.model.Meta;
import com.dewarim.cinnamon.model.MetasetType;
import com.dewarim.cinnamon.model.UserAccount;
import com.dewarim.cinnamon.model.response.MetaWrapper;
import com.dewarim.cinnamon.security.authorization.AuthorizationService;
import jakarta.servlet.http.HttpServlet;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.dewarim.cinnamon.DefaultPermission.BROWSE;
import static com.dewarim.cinnamon.ErrorCode.NO_READ_CUSTOM_METADATA_PERMISSION;

public class BaseServlet extends HttpServlet {

    static final AuthorizationService authorizationService = new AuthorizationService();

    static void throwUnlessSysMetadataIsReadable(Ownable ownable) {
        UserAccount user        = RequestScope.getCurrentUser();
        boolean     readAllowed = authorizationService.hasUserOrOwnerPermission(ownable, BROWSE, user);
        if (!readAllowed) {
            throw ErrorCode.NO_BROWSE_PERMISSION.getException().get();
        }
    }

    static void throwUnlessCustomMetaIsReadable(Ownable ownable) {
        UserAccount user = RequestScope.getCurrentUser();
        authorizationService.throwUpUnlessUserOrOwnerHasPermission(ownable,
                DefaultPermission.READ_OBJECT_CUSTOM_METADATA, user, NO_READ_CUSTOM_METADATA_PERMISSION);
    }

    static void createMetaResponse(CinnamonResponse response, List<Meta> metaList) {
        MetaWrapper wrapper = new MetaWrapper(metaList);
        response.setWrapper(wrapper);
    }

    static void checkMetaUniqueness(List<Meta> metas) {
        Set<Long> uniqueTypes = new MetasetTypeDao().list().stream().filter(MetasetType::getUnique).map(MetasetType::getId).collect(Collectors.toSet());
        boolean multipleUniques = metas.stream().map(Meta::getTypeId).anyMatch(typeId ->
                uniqueTypes.contains(typeId) && metas.stream().filter(meta -> meta.getTypeId().equals(typeId)).count() > 1
        );
        if (multipleUniques) {
            throw ErrorCode.METASET_UNIQUE_CHECK_FAILED.exception();
        }
    }


}
