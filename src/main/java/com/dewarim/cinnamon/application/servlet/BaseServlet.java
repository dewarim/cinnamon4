package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.DefaultPermission;
import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.Ownable;
import com.dewarim.cinnamon.application.CinnamonResponse;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.Meta;
import com.dewarim.cinnamon.model.UserAccount;
import com.dewarim.cinnamon.model.response.MetaWrapper;
import com.dewarim.cinnamon.security.authorization.AuthorizationService;
import jakarta.servlet.http.HttpServlet;

import java.util.List;

public class BaseServlet extends HttpServlet {

    static void throwUnlessSysMetadataIsWritable(Ownable ownable) {
        UserAccount user         = ThreadLocalSqlSession.getCurrentUser();
        boolean     writeAllowed = new AuthorizationService().hasUserOrOwnerPermission(ownable, DefaultPermission.WRITE_OBJECT_SYS_METADATA, user);
        if (!writeAllowed) {
            throw ErrorCode.NO_WRITE_SYS_METADATA_PERMISSION.getException().get();
        }
    }

    static void throwUnlessSysMetadataIsReadable(Ownable ownable) {
        UserAccount user        = ThreadLocalSqlSession.getCurrentUser();
        boolean     readAllowed = new AuthorizationService().hasUserOrOwnerPermission(ownable, DefaultPermission.READ_OBJECT_SYS_METADATA, user);
        if (!readAllowed) {
            throw ErrorCode.NO_READ_OBJECT_SYS_METADATA_PERMISSION.getException().get();
        }
    }

    static void throwUnlessCustomMetaIsReadable(Ownable ownable) {
        UserAccount user        = ThreadLocalSqlSession.getCurrentUser();
        boolean     readAllowed = new AuthorizationService().hasUserOrOwnerPermission(ownable, DefaultPermission.READ_OBJECT_CUSTOM_METADATA, user);
        if (!readAllowed) {
            throw ErrorCode.NO_READ_CUSTOM_METADATA_PERMISSION.getException().get();
        }
    }

    static void throwUnlessCustomMetaIsWritable(Ownable ownable, UserAccount user) {
        boolean readAllowed = new AuthorizationService().hasUserOrOwnerPermission(ownable, DefaultPermission.READ_OBJECT_CUSTOM_METADATA, user);
        if (!readAllowed) {
            throw ErrorCode.NO_WRITE_CUSTOM_METADATA_PERMISSION.getException().get();
        }
    }

    static void createMetaResponse( CinnamonResponse response, List<Meta> metaList) {
        MetaWrapper wrapper = new MetaWrapper(metaList);
        response.setWrapper(wrapper);
    }


}
