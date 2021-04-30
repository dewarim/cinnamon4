package com.dewarim.cinnamon;

import jakarta.servlet.http.HttpServletResponse;

import java.util.function.Supplier;


public enum ErrorCode {

    ACL_NOT_FOUND("error.acl.not_found", HttpServletResponse.SC_NOT_FOUND),
    AUTHENTICATION_FAIL_NO_TICKET_GIVEN("empty or null ticket given", HttpServletResponse.SC_FORBIDDEN),
    AUTHENTICATION_FAIL_NO_SESSION_FOUND("no session found", HttpServletResponse.SC_FORBIDDEN),
    AUTHENTICATION_FAIL_SESSION_EXPIRED("session expired", HttpServletResponse.SC_FORBIDDEN),
    AUTHENTICATION_FAIL_USER_NOT_FOUND("user not found or inactive", HttpServletResponse.SC_FORBIDDEN),
    CANNOT_MOVE_FOLDER_INTO_ITSELF("source and parent folder are identical", HttpServletResponse.SC_BAD_REQUEST),
    CANNOT_DELETE_DUE_TO_ERRORS("delete operation encountered errors", HttpServletResponse.SC_CONFLICT),
    CONNECTION_FAIL_ACCOUNT_INACTIVE("account inactive", HttpServletResponse.SC_UNAUTHORIZED),
    CONNECTION_FAIL_INVALID_USERNAME("invalid username", HttpServletResponse.SC_UNAUTHORIZED),
    CONNECTION_FAIL_ACCOUNT_LOCKED("account locked", HttpServletResponse.SC_UNAUTHORIZED),
    CONNECTION_FAIL_WRONG_PASSWORD("wrong password", HttpServletResponse.SC_UNAUTHORIZED),
    //    DB_UPDATE_CHANGED_NOTHING("The update succeeded, but did not change anything. This may happen when you save the same value again."),
    DB_UPDATE_FAILED("db update failed", HttpServletResponse.SC_INTERNAL_SERVER_ERROR),
    DB_DELETE_FAILED("db delete failed", HttpServletResponse.SC_INTERNAL_SERVER_ERROR),
    DB_INSERT_FAILED("db insert failed", HttpServletResponse.SC_INTERNAL_SERVER_ERROR),
    DB_IS_MISSING_LANGUAGE_CODE("db does not contain ISO code for undetermined language.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR),
    DELETE_AFFECTED_MULTIPLE_ROWS("Delete succeeded, but seems to have deleted more than the expected single row. Contact your administrator.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR),
    DELETE_REQUEST_WITHOUT_ID("delete request needs id parameter", HttpServletResponse.SC_BAD_REQUEST),
    FILE_NOT_FOUND("file not found", HttpServletResponse.SC_NOT_FOUND),
    DUPLICATE_FOLDER_NAME_FORBIDDEN("You cannot have two folders with the same name with the same parent folder", HttpServletResponse.SC_BAD_REQUEST),
    FOLDER_NOT_FOUND("folder was not found", HttpServletResponse.SC_NOT_FOUND),
    FOLDER_TYPE_NOT_FOUND("folder type was not found", HttpServletResponse.SC_NOT_FOUND),
    FORBIDDEN("user is authenticated, but access is not allowed", HttpServletResponse.SC_FORBIDDEN),
    FORMAT_NOT_FOUND("format object was not found for given id", HttpServletResponse.SC_NOT_FOUND),
    ID_PARAM_IS_INVALID("id param is missing or invalid", HttpServletResponse.SC_BAD_REQUEST),
    INFO_REQUEST_WITHOUT_NAME_OR_ID("request needs id or name parameter", HttpServletResponse.SC_BAD_REQUEST),
    INTERNAL_SERVER_ERROR_TRY_AGAIN_LATER("internal server  please retry later", HttpServletResponse.SC_INTERNAL_SERVER_ERROR),
    INVALID_FOLDER_PATH_STRUCTURE("Invalid folder path structure.", HttpServletResponse.SC_BAD_REQUEST),
    INVALID_LINK_RESOLVER("Links to folders must have LinkResolver.FIXED.", HttpServletResponse.SC_BAD_REQUEST),
    INVALID_ID_TYPE("Invalid id type in request object", HttpServletResponse.SC_BAD_REQUEST),
    INVALID_REQUEST("request is invalid check parameters", HttpServletResponse.SC_BAD_REQUEST),
    LANGUAGE_NOT_FOUND("language was not found in the database", HttpServletResponse.SC_NOT_FOUND),
    LIFECYCLE_NOT_FOUND("Lifecycle was not found in the database", HttpServletResponse.SC_NOT_FOUND),
    LIFECYCLE_STATE_CHANGE_FAILED("Lifecycle state change failed.", HttpServletResponse.SC_BAD_REQUEST),
    LIFECYCLE_STATE_EXIT_FAILED("Failed to exit existing lifecycle state.", HttpServletResponse.SC_BAD_REQUEST),
    LIFECYCLE_STATE_NOT_FOUND("Lifecycle state was not found in database", HttpServletResponse.SC_NOT_FOUND),
    LIFECYCLE_STATE_BY_NAME_NOT_FOUND("Named lifecycle state was not found in database", HttpServletResponse.SC_NOT_FOUND),
    LOGIN_FAILED("login failed", HttpServletResponse.SC_UNAUTHORIZED),
    METASET_IS_UNIQUE_AND_ALREADY_EXISTS("The metaset is already exists and is unique", HttpServletResponse.SC_BAD_REQUEST),
    METASET_NOT_FOUND("The Metaset was not found.", HttpServletResponse.SC_NOT_FOUND),
    METASET_TYPE_NOT_FOUND("metaset type was not found", HttpServletResponse.SC_NOT_FOUND),
    MISSING_FILE_PARAMETER("parameter 'file' for uploaded content is missing", HttpServletResponse.SC_BAD_REQUEST),
    MISSING_SET_ACL_PERMISSION("missing set_acl permission", HttpServletResponse.SC_UNAUTHORIZED),
    MISSING_WRITE_OBJECT_SYS_METADATA("missing write_object_sys_metadata", HttpServletResponse.SC_UNAUTHORIZED),
    NAME_PARAM_IS_INVALID("name param is invalid", HttpServletResponse.SC_BAD_REQUEST),
    MISSING_REQUEST_PAYLOAD("request is missing request data", HttpServletResponse.SC_BAD_REQUEST),
    NO_BROWSE_PERMISSION("missing browse permission", HttpServletResponse.SC_UNAUTHORIZED),
    NO_CONTENT_TYPE_IN_HEADER("missing content-type field in header", HttpServletResponse.SC_BAD_REQUEST),
    NO_CREATE_PERMISSION("missing permission to create an object inside a folder", HttpServletResponse.SC_UNAUTHORIZED),
    NO_DELETE_PERMISSION("missing permission to delete this item", HttpServletResponse.SC_UNAUTHORIZED),
    NO_EDIT_FOLDER_PERMISSION("missing permission to edit the folder", HttpServletResponse.SC_UNAUTHORIZED),
    NO_LOCK_PERMISSION("missing permission to (un)lock this object", HttpServletResponse.SC_UNAUTHORIZED),
    NO_MOVE_PERMISSION("missing permission to move object", HttpServletResponse.SC_UNAUTHORIZED),
    NO_READ_PERMISSION("missing read content permission for current object", HttpServletResponse.SC_UNAUTHORIZED),
    NO_READ_CUSTOM_METADATA_PERMISSION("missing permission to read custom metadata", HttpServletResponse.SC_UNAUTHORIZED),
    NO_READ_OBJECT_SYS_METADATA_PERMISSION("missing permission to read system metadata", HttpServletResponse.SC_UNAUTHORIZED),
    NO_VERSION_PERMISSION("missing permission to version target object", HttpServletResponse.SC_UNAUTHORIZED),
    NO_WRITE_CUSTOM_METADATA_PERMISSION("missing permission to write custom metadata", HttpServletResponse.SC_UNAUTHORIZED),
    NO_WRITE_PERMISSION("missing write content permission for current object", HttpServletResponse.SC_UNAUTHORIZED),
    NO_WRITE_SYS_METADATA_PERMISSION("Required permission to write system metadata was not found.", HttpServletResponse.SC_UNAUTHORIZED),
    NOT_MULTIPART_UPLOAD("the request must have the contentType multipart/form-data", HttpServletResponse.SC_BAD_REQUEST),
    OBJECT_HAS_DESCENDANTS("this object has descendants", HttpServletResponse.SC_BAD_REQUEST),
    OBJECT_HAS_NO_CONTENT("this object has no content", HttpServletResponse.SC_NOT_FOUND),
    OBJECT_HAS_PROTECTED_RELATIONS("this object has protected relations", HttpServletResponse.SC_UNAUTHORIZED),
    OBJECT_HAS_VANISHED_DURING_UPDATE("the object the you wanted to change was not found in the database anymore (maybe someone deleted it?)", HttpServletResponse.SC_NOT_FOUND),
    OBJECT_LOCKED_BY_OTHER_USER("object is locked by another user", HttpServletResponse.SC_FORBIDDEN),
    OBJECT_MUST_BE_LOCKED_BY_USER("object must be locked by current user before setContent is allowed", HttpServletResponse.SC_FORBIDDEN),
    OBJECT_NOT_FOUND("object not found", HttpServletResponse.SC_NOT_FOUND),
    OBJECT_NOT_FOUND_OR_GONE("object not found (perhaps already deleted)", HttpServletResponse.SC_NOT_FOUND),
    OBJECT_WITH_FILE_NEEDS_FORMAT("object with file data must have valid format", HttpServletResponse.SC_BAD_REQUEST),
    OBJECT_TYPE_NOT_FOUND("object type was not found", HttpServletResponse.SC_NOT_FOUND),
    OWNER_NOT_FOUND("owner not found", HttpServletResponse.SC_BAD_REQUEST),
    PASSWORD_TOO_SHORT("password is too short - default minimum length is 8", HttpServletResponse.SC_BAD_REQUEST),
    PARENT_FOLDER_NOT_FOUND("parent folder not found", HttpServletResponse.SC_BAD_REQUEST),
    PERMISSIONS_NOT_FOUND("no permissions found for user and acl", HttpServletResponse.SC_BAD_REQUEST),
    RELATION_TYPE_NOT_FOUND("RelationType was not found.", HttpServletResponse.SC_NOT_FOUND),
    REQUIRES_SUPERUSER_STATUS("action requires superuser status", HttpServletResponse.SC_FORBIDDEN),
    SESSION_NOT_FOUND("session not found", HttpServletResponse.SC_NOT_FOUND),
    STATIC__NO_PATH_TRAVERSAL("no path traversal allowed", HttpServletResponse.SC_FORBIDDEN),
    UNAUTHORIZED("access not allowed", HttpServletResponse.SC_UNAUTHORIZED),
    USER_INFO_REQUEST_WITHOUT_NAME_OR_ID("userInfoRequest missing id or name", HttpServletResponse.SC_BAD_REQUEST),
    USER_ACCOUNT_NOT_FOUND("userInfoRequest invalid id or name", HttpServletResponse.SC_NOT_FOUND);

    String                           description;
    int                              httpResponseCode;
    Supplier<FailedRequestException> exceptionSupplier;

    ErrorCode(String description, int httpResponseCode) {
        this.description = description;
        this.httpResponseCode = httpResponseCode;
        this.exceptionSupplier = () -> new FailedRequestException(this);
    }

    public String getCode() {
        return name();
    }

    public int getHttpResponseCode() {
        return httpResponseCode;
    }

    public Supplier<FailedRequestException> getException() {
        return exceptionSupplier;
    }

    public FailedRequestException exception() {
        return exceptionSupplier.get();
    }

    public void throwUp() {
        throw exceptionSupplier.get();
    }

}
