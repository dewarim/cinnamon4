package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.DefaultPermission;
import com.dewarim.cinnamon.api.Ownable;
import com.dewarim.cinnamon.application.ErrorCode;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.UserAccount;
import com.dewarim.cinnamon.security.authorization.AuthorizationService;

import javax.servlet.http.HttpServlet;

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

}
