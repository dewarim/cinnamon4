package com.dewarim.cinnamon.application;

public enum ErrorCode {
    
    AUTHENTICATION_FAIL_NO_TICKET_GIVEN("empty or null ticket given"),
    AUTHENTICATION_FAIL_NO_SESSION_FOUND("no session found"),
    AUTHENTICATION_FAIL_SESSION_EXPIRED("session expired"),
    AUTHENTICATION_FAIL_USER_NOT_FOUND("user not found or inactive"),
    CONNECTION_FAIL_ACCOUNT_INACTIVE("account inactive"),
    CONNECTION_FAIL_INVALID_USERNAME("invalid username"),
    CONNECTION_FAIL_ACCOUNT_LOCKED("account locked"),
    CONNECTION_FAIL_WRONG_PASSWORD("wrong password"),
    INTERNAL_SERVER_ERROR_TRY_AGAIN_LATER("internal server  please retry later"),
    USER_INFO_REQUEST_WITHOUT_NAME_OR_ID("userInfoRequest missing id or name"),
    USER_ACCOUNT_NOT_FOUND("userInfoRequest invalid id or name"),
    LOGIN_FAILED("login failed"),
    REQUIRES_SUPERUSER_STATUS("action requires superuser status"),
    NAME_PARAM_IS_INVALID("name param is invalid"),
    INFO_REQUEST_WITHOUT_NAME_OR_ID("request needs id or name parameter"),
    DELETE_REQUEST_WITHOUT_ID("delete request needs id parameter"),
    DB_UPDATE_FAILED("db update failed"),
    DB_UPDATE_CHANGED_NOTHING("The update succeeded, but did not change anything. This may happen when you save the same value again."),
    ID_PARAM_IS_INVALID("id param is missing or invalid"),
    OBJECT_NOT_FOUND("object not found"),
    PERMISSIONS_NOT_FOUND("no permissions found for user and acl"),
    UNAUTHORIZED("access not allowed"), 
    INVALID_REQUEST("request is invalid check parameters"),
    PARENT_FOLDER_NOT_FOUND("parent folder not found"), 
    ACL_NOT_FOUND("acl not found"),
    SESSION_NOT_FOUND("session not found"),
    STATIC__NO_PATH_TRAVERSAL("no path traversal allowed"),
    OWNER_NOT_FOUND("owner not found"),
    FILE_NOT_FOUND("file not found"),
    MISSING_SET_ACL_PERMISSION("missing set_acl permission"),
    MISSING_WRITE_OBJECT_SYS_METADATA("missing write_object_sys_metadata"), 
    OBJECT_HAS_VANISHED_DURING_UPDATE("the object the you wanted to change was not found in the database anymore (maybe someone deleted it?)"), 
    FOLDER_NOT_FOUND("folder was not found"), 
    NO_BROWSE_PERMISSION("missing browse permission"), 
    NO_CREATE_PERMISSION("missing permission to create an object inside a folder"),
    INVALID_LINK_RESOLVER("Links to folders must have LinkResolver.FIXED.");
    
    String code;

    ErrorCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
