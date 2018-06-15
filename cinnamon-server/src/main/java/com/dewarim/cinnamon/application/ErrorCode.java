package com.dewarim.cinnamon.application;

public enum ErrorCode {

    ACL_NOT_FOUND("acl not found"),
    AUTHENTICATION_FAIL_NO_TICKET_GIVEN("empty or null ticket given"),
    AUTHENTICATION_FAIL_NO_SESSION_FOUND("no session found"),
    AUTHENTICATION_FAIL_SESSION_EXPIRED("session expired"),
    AUTHENTICATION_FAIL_USER_NOT_FOUND("user not found or inactive"),
    CONNECTION_FAIL_ACCOUNT_INACTIVE("account inactive"),
    CONNECTION_FAIL_INVALID_USERNAME("invalid username"),
    CONNECTION_FAIL_ACCOUNT_LOCKED("account locked"),
    CONNECTION_FAIL_WRONG_PASSWORD("wrong password"),
    DB_UPDATE_CHANGED_NOTHING("The update succeeded, but did not change anything. This may happen when you save the same value again."),
    DB_UPDATE_FAILED("db update failed"),
    // From the "this cannot happen" department:
    DELETE_AFFECTED_MULTIPLE_ROWS("Delete succeeded, but seems to have deleted more than the expected single row. Contact your administrator."),
    DELETE_REQUEST_WITHOUT_ID("delete request needs id parameter"),
    FILE_NOT_FOUND("file not found"),
    FOLDER_NOT_FOUND("folder was not found"),
    FORBIDDEN("user is authenticated, but access is not allowed"),
    FORMAT_NOT_FOUND("format object was not found for given id"),
    ID_PARAM_IS_INVALID("id param is missing or invalid"),
    INFO_REQUEST_WITHOUT_NAME_OR_ID("request needs id or name parameter"),
    INTERNAL_SERVER_ERROR_TRY_AGAIN_LATER("internal server  please retry later"),
    INVALID_LINK_RESOLVER("Links to folders must have LinkResolver.FIXED."),
    INVALID_REQUEST("request is invalid check parameters"),
    LOGIN_FAILED("login failed"),
    MISSING_FILE_PARAMETER("parameter 'file' for uploaded content is missing"),
    MISSING_SET_ACL_PERMISSION("missing set_acl permission"),
    MISSING_WRITE_OBJECT_SYS_METADATA("missing write_object_sys_metadata"),
    NAME_PARAM_IS_INVALID("name param is invalid"),
    NO_BROWSE_PERMISSION("missing browse permission"),
    NO_CREATE_PERMISSION("missing permission to create an object inside a folder"),
    NO_READ_PERMISSION("missing read content permission for current object"),
    NO_WRITE_PERMISSION("missing write content permission for current object"),
    NO_WRITE_SYS_METADATA_PERMISSION("Required permission to write system metadata was not found."),
    NOT_MULTIPART_UPLOAD("the request must have the contentType multipart/form-data"),
    OBJECT_HAS_NO_CONTENT("this object has no content"),
    OBJECT_HAS_VANISHED_DURING_UPDATE("the object the you wanted to change was not found in the database anymore (maybe someone deleted it?)"),
    OBJECT_NOT_FOUND("object not found"),
    OBJECT_NOT_FOUND_OR_GONE("object not found (perhaps already deleted)"),
    OWNER_NOT_FOUND("owner not found"),
    PASSWORD_TOO_SHORT("password is too short - default minimum length is 8"),
    PARENT_FOLDER_NOT_FOUND("parent folder not found"),
    PERMISSIONS_NOT_FOUND("no permissions found for user and acl"),
    RELATION_TYPE_NOT_FOUND("RelationType was not found."),
    REQUIRES_SUPERUSER_STATUS("action requires superuser status"),
    SESSION_NOT_FOUND("session not found"),
    STATIC__NO_PATH_TRAVERSAL("no path traversal allowed"),
    UNAUTHORIZED("access not allowed"),
    USER_INFO_REQUEST_WITHOUT_NAME_OR_ID("userInfoRequest missing id or name"),
    USER_ACCOUNT_NOT_FOUND("userInfoRequest invalid id or name");

    String code;

    ErrorCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public enum OBJECT_NOT_FOUND_OR_GONE {}
}
