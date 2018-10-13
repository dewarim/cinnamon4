package com.dewarim.cinnamon.application;

import com.dewarim.cinnamon.application.exception.FailedRequestException;

import java.util.function.Supplier;

import static javax.servlet.http.HttpServletResponse.*;

public enum ErrorCode {

    ACL_NOT_FOUND("error.acl.not_found", SC_NOT_FOUND),
    AUTHENTICATION_FAIL_NO_TICKET_GIVEN("empty or null ticket given",SC_FORBIDDEN),
    AUTHENTICATION_FAIL_NO_SESSION_FOUND("no session found",SC_FORBIDDEN),
    AUTHENTICATION_FAIL_SESSION_EXPIRED("session expired",SC_FORBIDDEN),
    AUTHENTICATION_FAIL_USER_NOT_FOUND("user not found or inactive",SC_FORBIDDEN),
    CONNECTION_FAIL_ACCOUNT_INACTIVE("account inactive",SC_UNAUTHORIZED),
    CONNECTION_FAIL_INVALID_USERNAME("invalid username",SC_UNAUTHORIZED),
    CONNECTION_FAIL_ACCOUNT_LOCKED("account locked",SC_UNAUTHORIZED),
    CONNECTION_FAIL_WRONG_PASSWORD("wrong password",SC_UNAUTHORIZED),
//    DB_UPDATE_CHANGED_NOTHING("The update succeeded, but did not change anything. This may happen when you save the same value again."),
    DB_UPDATE_FAILED("db update failed", SC_INTERNAL_SERVER_ERROR),
    // From the "this cannot happen" department:
    DELETE_AFFECTED_MULTIPLE_ROWS("Delete succeeded, but seems to have deleted more than the expected single row. Contact your administrator.",SC_INTERNAL_SERVER_ERROR),
    DELETE_REQUEST_WITHOUT_ID("delete request needs id parameter", SC_BAD_REQUEST),
    FILE_NOT_FOUND("file not found", SC_NOT_FOUND),
    FOLDER_NOT_FOUND("folder was not found", SC_NOT_FOUND),
    FORBIDDEN("user is authenticated, but access is not allowed", SC_FORBIDDEN),
    FORMAT_NOT_FOUND("format object was not found for given id", SC_NOT_FOUND),
    ID_PARAM_IS_INVALID("id param is missing or invalid", SC_BAD_REQUEST),
    INFO_REQUEST_WITHOUT_NAME_OR_ID("request needs id or name parameter", SC_BAD_REQUEST),
    INTERNAL_SERVER_ERROR_TRY_AGAIN_LATER("internal server  please retry later", SC_INTERNAL_SERVER_ERROR),
    INVALID_FOLDER_PATH_STRUCTURE("Invalid folder path structure.", SC_BAD_REQUEST),
    INVALID_LINK_RESOLVER("Links to folders must have LinkResolver.FIXED.", SC_BAD_REQUEST),
    INVALID_REQUEST("request is invalid check parameters", SC_BAD_REQUEST),
    LIFECYCLE_NOT_FOUND("Lifecycle was not found in the database", SC_NOT_FOUND),
    LIFECYCLE_STATE_CHANGE_FAILED("Lifecycle state change failed.", SC_BAD_REQUEST),
    LIFECYCLE_STATE_EXIT_FAILED("Failed to exit existing lifecycle state.", SC_BAD_REQUEST),
    LIFECYCLE_STATE_NOT_FOUND("Lifecycle state was not found in database", SC_NOT_FOUND),
    LIFECYCLE_STATE_BY_NAME_NOT_FOUND("Named lifecycle state was not found in database", SC_NOT_FOUND),
    LOGIN_FAILED("login failed", SC_UNAUTHORIZED),
    MISSING_FILE_PARAMETER("parameter 'file' for uploaded content is missing", SC_BAD_REQUEST),
    MISSING_SET_ACL_PERMISSION("missing set_acl permission", SC_UNAUTHORIZED),
    MISSING_WRITE_OBJECT_SYS_METADATA("missing write_object_sys_metadata", SC_UNAUTHORIZED),
    NAME_PARAM_IS_INVALID("name param is invalid", SC_BAD_REQUEST),
    NO_BROWSE_PERMISSION("missing browse permission", SC_UNAUTHORIZED),
    NO_CREATE_PERMISSION("missing permission to create an object inside a folder", SC_UNAUTHORIZED),
    NO_LOCK_PERMISSION("missing permission to (un)lock this object", SC_UNAUTHORIZED),
    NO_READ_PERMISSION("missing read content permission for current object", SC_UNAUTHORIZED),
    NO_WRITE_PERMISSION("missing write content permission for current object", SC_UNAUTHORIZED),
    NO_WRITE_SYS_METADATA_PERMISSION("Required permission to write system metadata was not found.", SC_UNAUTHORIZED),
    NOT_MULTIPART_UPLOAD("the request must have the contentType multipart/form-data", SC_BAD_REQUEST),
    OBJECT_HAS_NO_CONTENT("this object has no content", SC_NOT_FOUND),
    OBJECT_HAS_VANISHED_DURING_UPDATE("the object the you wanted to change was not found in the database anymore (maybe someone deleted it?)", SC_NOT_FOUND),
    OBJECT_LOCKED_BY_OTHER_USER("object is locked by another user", SC_FORBIDDEN),
    OBJECT_NOT_FOUND("object not found", SC_NOT_FOUND),
    OBJECT_NOT_FOUND_OR_GONE("object not found (perhaps already deleted)", SC_NOT_FOUND),
    OWNER_NOT_FOUND("owner not found", SC_BAD_REQUEST),
    PASSWORD_TOO_SHORT("password is too short - default minimum length is 8", SC_BAD_REQUEST),
    PARENT_FOLDER_NOT_FOUND("parent folder not found", SC_BAD_REQUEST),
    PERMISSIONS_NOT_FOUND("no permissions found for user and acl", SC_BAD_REQUEST),
    RELATION_TYPE_NOT_FOUND("RelationType was not found.", SC_NOT_FOUND),
    REQUIRES_SUPERUSER_STATUS("action requires superuser status", SC_FORBIDDEN),
    SESSION_NOT_FOUND("session not found", SC_NOT_FOUND),
    STATIC__NO_PATH_TRAVERSAL("no path traversal allowed", SC_FORBIDDEN),
    UNAUTHORIZED("access not allowed", SC_UNAUTHORIZED),
    USER_INFO_REQUEST_WITHOUT_NAME_OR_ID("userInfoRequest missing id or name", SC_BAD_REQUEST),
    USER_ACCOUNT_NOT_FOUND("userInfoRequest invalid id or name", SC_NOT_FOUND);

    String code;
    int httpResponseCode;
    Supplier<FailedRequestException> exceptionSupplier;

    ErrorCode(String code, int httpResponseCode) {
        this.code = code;
        this.httpResponseCode = httpResponseCode;
        this.exceptionSupplier = ()-> new FailedRequestException(this);
    }

    public String getCode() {
        return code;
    }

    public int getHttpResponseCode() {
        return httpResponseCode;
    }

    public Supplier<FailedRequestException> getException() {
        return exceptionSupplier;
    }
}
