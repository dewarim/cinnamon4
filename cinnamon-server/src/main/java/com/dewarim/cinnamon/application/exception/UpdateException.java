package com.dewarim.cinnamon.application.exception;

import com.dewarim.cinnamon.ErrorCode;

import static jakarta.servlet.http.HttpServletResponse.*;


public class UpdateException extends RuntimeException{

    public static final UpdateException ACL_NOT_FOUND = new UpdateException(SC_NOT_FOUND, ErrorCode.ACL_NOT_FOUND);
    public static final UpdateException MISSING_SET_ACL_PERMISSION = new UpdateException(SC_UNAUTHORIZED, ErrorCode.MISSING_SET_ACL_PERMISSION);
    public static final UpdateException MISSING_SET_SYSMETA_PERMISSION = new UpdateException(SC_UNAUTHORIZED, ErrorCode.MISSING_WRITE_OBJECT_SYS_METADATA);
    public static final UpdateException DB_UPDATE_CHANGED_NOTHING = new UpdateException(SC_INTERNAL_SERVER_ERROR, ErrorCode.DB_UPDATE_FAILED);
    public static final UpdateException OBJECT_HAS_VANISHED = new UpdateException(SC_NOT_FOUND, ErrorCode.OBJECT_HAS_VANISHED_DURING_UPDATE);
    public static final UpdateException FOLDER_NOT_FOUND = new UpdateException(SC_NOT_FOUND, ErrorCode.FOLDER_NOT_FOUND);
    public static final UpdateException NO_BROWSE_PERMISSION = new UpdateException(SC_UNAUTHORIZED, ErrorCode.NO_BROWSE_PERMISSION);
    public static final UpdateException OBJECT_NOT_FOUND = new UpdateException(SC_NOT_FOUND, ErrorCode.OBJECT_NOT_FOUND);
    public static final UpdateException USER_NOT_FOUND = new UpdateException(SC_NOT_FOUND, ErrorCode.USER_ACCOUNT_NOT_FOUND);
    public static final UpdateException NO_CREATE_PERMISSION = new UpdateException(SC_UNAUTHORIZED, ErrorCode.NO_CREATE_PERMISSION);
    public static final UpdateException INVALID_LINK_RESOLVER = new UpdateException(SC_BAD_REQUEST, ErrorCode.INVALID_LINK_RESOLVER);
    
    
    
    private final int       statusCode;
    private final ErrorCode errorCode;

    public UpdateException(int statusCode, ErrorCode errorCode) {
        super();
        this.errorCode = errorCode;
        this.statusCode = statusCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
