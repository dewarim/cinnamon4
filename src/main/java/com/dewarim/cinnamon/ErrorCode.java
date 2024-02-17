package com.dewarim.cinnamon;

import com.dewarim.cinnamon.api.Constants;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static jakarta.servlet.http.HttpServletResponse.*;


public enum ErrorCode {

    ACL_GROUP_NOT_FOUND("the requested acl group was not found in the database", HttpServletResponse.SC_NOT_FOUND),
    ACL_NOT_FOUND("error.acl.not_found", HttpServletResponse.SC_NOT_FOUND),
    AUTHENTICATION_FAIL_NO_SESSION_FOUND("no session found", HttpServletResponse.SC_FORBIDDEN),
    AUTHENTICATION_FAIL_NO_TICKET_GIVEN("empty or null ticket given", HttpServletResponse.SC_FORBIDDEN),
    AUTHENTICATION_FAIL_SESSION_EXPIRED("session expired", HttpServletResponse.SC_FORBIDDEN),
    AUTHENTICATION_FAIL_USER_NOT_FOUND("user not found or inactive", HttpServletResponse.SC_FORBIDDEN),
    CANNOT_CHANGE_LINK_TYPE("Will not change link type: please delete old link and create a new one.", SC_BAD_REQUEST),
    CANNOT_DELETE_DUE_TO_ERRORS("delete operation encountered errors", HttpServletResponse.SC_CONFLICT),
    CANNOT_MOVE_FOLDER_INTO_ITSELF("source and parent folder are identical", SC_BAD_REQUEST),
    CHANGED_FLAG_ONLY_USABLE_BY_UNTRACKED_USERS("only users without change-tracking may update metadataChanged and contentChanged fields", SC_FORBIDDEN),
    CONNECTION_FAIL_ACCOUNT_INACTIVE("account inactive", SC_UNAUTHORIZED),
    CONNECTION_FAIL_ACCOUNT_LOCKED("account locked", SC_UNAUTHORIZED),
    CONNECTION_FAIL_INVALID_USERNAME("invalid username", SC_UNAUTHORIZED),
    CONNECTION_FAIL_WRONG_PASSWORD("authentication failed (wrong password or account does not exist?)", SC_UNAUTHORIZED),
    COPY_TO_EXISTING_INCONSISTENCY("""
            number of source/target objects found in database is inconsistent (not all objects for copying or all targets were found - or you have duplicate source/target ids)
            """, SC_BAD_REQUEST),
    COPY_TO_EXISTING_FAILED("""
            Failed to copy to existing object(s), most likely due to missing permission - see included error list for more details.
            """, SC_BAD_REQUEST),
    COPY_TO_EXISTING_OVERLAP("""
            When copying from a list of source OSDs to existing target objects, the lists must not have any overlap.
            """, SC_BAD_REQUEST),
    DB_DELETE_FAILED("db delete failed", HttpServletResponse.SC_INTERNAL_SERVER_ERROR),
    DB_INSERT_FAILED("db insert failed", HttpServletResponse.SC_INTERNAL_SERVER_ERROR),
    DB_IS_MISSING_LANGUAGE_CODE("db does not contain ISO code for undetermined language.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR),
    DB_UPDATE_FAILED("db update failed", HttpServletResponse.SC_INTERNAL_SERVER_ERROR) //    DB_UPDATE_CHANGED_NOTHING("The update succeeded, but did not change anything. This may happen when you save the same value again."),
    ,

    DELETE_AFFECTED_MULTIPLE_ROWS("Delete succeeded, but seems to have deleted more than the expected single row. Contact your administrator.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR),
    DELETE_REQUEST_WITHOUT_ID("delete request needs id parameter", SC_BAD_REQUEST),
    DUPLICATE_FOLDER_NAME_FORBIDDEN("You cannot have two folders with the same name with the same parent folder", SC_BAD_REQUEST),
    FILE_NOT_FOUND("file not found", HttpServletResponse.SC_NOT_FOUND),
    FOLDER_HAS_SUBFOLDERS("folder has subfolders - will not delete unless deleteRecursively=true", HttpServletResponse.SC_FORBIDDEN),
    FOLDER_IS_NOT_EMPTY("folder has objects inside - will not delete non-empty folder deleteContent=true", HttpServletResponse.SC_FORBIDDEN),
    FOLDER_NOT_FOUND("folder was not found", HttpServletResponse.SC_NOT_FOUND),
    FOLDER_TYPE_NOT_FOUND("folder type was not found", HttpServletResponse.SC_NOT_FOUND),
    FORBIDDEN("user is authenticated, but access is not allowed", HttpServletResponse.SC_FORBIDDEN),
    FORMAT_NOT_FOUND("format object was not found for given id", HttpServletResponse.SC_NOT_FOUND),
    ID_PARAM_IS_INVALID("id param is missing or invalid", SC_BAD_REQUEST),
    ILLEGAL_STATE("reached illegal state, please contact administrator", HttpServletResponse.SC_INTERNAL_SERVER_ERROR),
    INTERNAL_SERVER_ERROR_TRY_AGAIN_LATER("internal server  please retry later", HttpServletResponse.SC_INTERNAL_SERVER_ERROR),
    INVALID_FOLDER_PATH_STRUCTURE("Invalid folder path structure.", SC_BAD_REQUEST),
    INVALID_ID_TYPE("Invalid id type in request object", SC_BAD_REQUEST),
    INVALID_LINK_RESOLVER("Links to folders must have LinkResolver.FIXED.", SC_BAD_REQUEST),
    INVALID_REQUEST("request is invalid check parameters", SC_BAD_REQUEST),
    INVALID_UPDATE("You are trying to update an object in a way that is not implemented yet, for example changing the type of a folder's metaset",
            SC_BAD_REQUEST),
    LANGUAGE_NOT_FOUND("language was not found in the database", HttpServletResponse.SC_NOT_FOUND),
    LDAP_CONNECTOR_NOT_CONFIGURED("ldap connector is not configured correctly", SC_CONFLICT),
    LDAP_LOGIN_FAILED("ldap user not found or login failed", SC_UNAUTHORIZED),
    LIFECYCLE_NOT_FOUND("Lifecycle was not found in the database", HttpServletResponse.SC_NOT_FOUND),
    LIFECYCLE_STATE_CHANGE_FAILED("Lifecycle state change failed.", SC_BAD_REQUEST),
    LIFECYCLE_STATE_EXIT_FAILED("Failed to exit existing lifecycle state.", SC_BAD_REQUEST),
    LIFECYCLE_STATE_NOT_FOUND("Lifecycle state was not found in database", HttpServletResponse.SC_NOT_FOUND),
    LOGIN_FAILED("login failed", SC_UNAUTHORIZED),
    LOGIN_TYPE_IS_UNKNOWN("the login type is unknown", SC_BAD_REQUEST),
    METASET_IS_UNIQUE_AND_ALREADY_EXISTS("The metaset is already exists and is unique", SC_BAD_REQUEST),
    METASET_NOT_FOUND("The Metaset was not found.", HttpServletResponse.SC_NOT_FOUND),
    METASET_TYPE_NOT_FOUND("metaset type was not found", HttpServletResponse.SC_NOT_FOUND),
    METASET_UNIQUE_CHECK_FAILED("Cannot create multiple unique metasets", SC_BAD_REQUEST),
    MISSING_FILE_PARAMETER("parameter 'file' for uploaded content is missing", SC_BAD_REQUEST),
    MISSING_REQUEST_PAYLOAD("request is missing request data", SC_BAD_REQUEST),
    MISSING_SET_ACL_PERMISSION("missing set_acl permission", SC_UNAUTHORIZED),
    MISSING_WRITE_OBJECT_SYS_METADATA("missing write_object_sys_metadata", SC_UNAUTHORIZED),
    NAME_PARAM_IS_INVALID("name param is invalid", SC_BAD_REQUEST),
    NOT_MULTIPART_UPLOAD("the request must have the contentType multipart/form-data", SC_BAD_REQUEST),
    NO_BROWSE_PERMISSION("missing browse permission", SC_UNAUTHORIZED),
    NO_CONTENT_TYPE_IN_HEADER("missing content-type field in header", SC_BAD_REQUEST),
    NO_CREATE_PERMISSION("missing permission to create an object inside a folder", SC_UNAUTHORIZED),
    NO_DELETE_LINK_PERMISSION("missing permission to delete this link", SC_UNAUTHORIZED),
    NO_DELETE_PERMISSION("missing permission to delete this item", SC_UNAUTHORIZED),
    NO_EDIT_FOLDER_PERMISSION("missing permission to edit the folder", SC_UNAUTHORIZED),
    NO_LIFECYCLE_STATE_WRITE_PERMISSION("missing permission to set lifecycle state", SC_UNAUTHORIZED),
    NO_LOCK_PERMISSION("missing permission to (un)lock this object", SC_UNAUTHORIZED),
    NO_NAME_WRITE_PERMISSION("missing permission to rename object", SC_UNAUTHORIZED),
    NO_READ_CUSTOM_METADATA_PERMISSION("missing permission to read custom metadata", SC_UNAUTHORIZED),
    NO_READ_OBJECT_SYS_METADATA_PERMISSION("missing permission to read system metadata", SC_UNAUTHORIZED),
    NO_READ_PERMISSION("missing read content permission for current object", SC_UNAUTHORIZED),
    NO_RELATION_CHILD_ADD_PERMISSION("missing relation.child.add permission", SC_UNAUTHORIZED),
    NO_RELATION_CHILD_REMOVE_PERMISSION("missing relation.child.remove permission", SC_UNAUTHORIZED),
    NO_RELATION_PARENT_ADD_PERMISSION("missing relation.parent.add permission", SC_UNAUTHORIZED),
    NO_RELATION_PARENT_REMOVE_PERMISSION("missing relation.parent.remove permission", SC_UNAUTHORIZED),
    NO_SET_LINK_TARGET_PERMISSION("You need the " + DefaultPermission.SET_LINK_TARGET.getName() + " permission to change a link's target.", SC_UNAUTHORIZED),
    NO_SET_OWNER_PERMISSION("You need the " + DefaultPermission.SET_OWNER.getName() + " permission to change an object's owner.",
            SC_UNAUTHORIZED),
    NO_SET_PARENT_PERMISSION("missing permission to move object", SC_UNAUTHORIZED),
    NO_SET_SUMMARY_PERMISSION("You need the " + DefaultPermission.SET_SUMMARY.getName() + " permission to update an object's summary.", SC_UNAUTHORIZED),
    NO_TYPE_WRITE_PERMISSION("missing permission change object's type", SC_UNAUTHORIZED),
    NO_UPDATE_LANGUAGE_PERMISSION("You need the " + DefaultPermission.SET_LANGUAGE.getName() + " permission to change an OSDs language.",
            SC_UNAUTHORIZED),
    NO_VERSION_PERMISSION("missing permission to version target object", SC_UNAUTHORIZED),
    NO_WRITE_CUSTOM_METADATA_PERMISSION("missing permission to write custom metadata", SC_UNAUTHORIZED),
    NO_WRITE_PERMISSION("missing write content permission for current object", SC_UNAUTHORIZED),
    OBJECT_HAS_DESCENDANTS("this object has descendants", SC_BAD_REQUEST),
    OBJECT_HAS_NO_CONTENT("this object has no content", HttpServletResponse.SC_NOT_FOUND),
    OBJECT_HAS_PROTECTED_RELATIONS("this object has protected relations", SC_UNAUTHORIZED),
    OBJECT_HAS_VANISHED_DURING_UPDATE("the object the you wanted to change was not found in the database anymore (maybe someone deleted it?)", HttpServletResponse.SC_NOT_FOUND),
    OBJECT_LOCKED_BY_OTHER_USER("object is locked by another user", HttpServletResponse.SC_FORBIDDEN),
    OBJECT_MUST_BE_LOCKED_BY_USER("object must be locked by current user before setContent is allowed", HttpServletResponse.SC_FORBIDDEN),
    OBJECT_NOT_FOUND("object not found", HttpServletResponse.SC_NOT_FOUND),
    OBJECT_NOT_FOUND_OR_GONE("object not found (perhaps already deleted)", HttpServletResponse.SC_NOT_FOUND),
    OBJECT_TYPE_NOT_FOUND("object type was not found", HttpServletResponse.SC_NOT_FOUND),
    OBJECT_WITH_FILE_NEEDS_FORMAT("object with file data must have valid format", SC_BAD_REQUEST),
    OWNER_NOT_FOUND("owner not found", SC_BAD_REQUEST),
    PARENT_FOLDER_NOT_FOUND("parent folder not found", SC_BAD_REQUEST),
    PASSWORD_TOO_SHORT("password is too short - default minimum length is 8", SC_BAD_REQUEST),
    PERMISSIONS_NOT_FOUND("no permissions found for user and acl", SC_BAD_REQUEST),
    REQUEST_DENIED_BY_CHANGE_TRIGGER("The request was blocked by a change trigger event.", HttpServletResponse.SC_FORBIDDEN),
    RELATION_TYPE_NOT_FOUND("RelationType was not found.", HttpServletResponse.SC_NOT_FOUND),
    REQUIRES_SUPERUSER_STATUS("action requires superuser status", HttpServletResponse.SC_FORBIDDEN),
    RESOURCE_NOT_FOUND("No code path maps to this URL path", HttpServletResponse.SC_NOT_FOUND),
    SESSION_NOT_FOUND("session not found", HttpServletResponse.SC_NOT_FOUND),
    STATIC__NO_PATH_TRAVERSAL("no path traversal allowed", HttpServletResponse.SC_FORBIDDEN),
    UNAUTHORIZED("access not allowed", SC_UNAUTHORIZED),
    /**
     * Only to be used as default value if no other error is specified (see: CinnamonClientException with causes != known errors)
     */
    UNKNOWN_ERROR_TYPE("undefined error", HttpServletResponse.SC_INTERNAL_SERVER_ERROR),
    USER_ACCOUNT_NOT_FOUND("userInfoRequest invalid id or name", HttpServletResponse.SC_NOT_FOUND),
    USER_ACCOUNT_SET_PASSWORD_NOT_ALLOWED("""
            You can only set a password on a user with original Cinnamon login,
             Cinnamon cannot change your password on an external login provider like LDAP.""", SC_BAD_REQUEST),

    USER_INFO_REQUEST_WITHOUT_NAME_OR_ID("userInfoRequest missing id or name", SC_BAD_REQUEST),
    GROUP_HAS_CHILDREN("Group cannot be deleted, it still has child groups and deleteChildren flag is not set.", SC_CONFLICT),
    DELETE_USER_NEEDS_ASSET_RECEIVER("""
            You must submit the user id of another user who stands to receive ownership of all
            assets belonging to the deleted user, if any.
            """, SC_BAD_REQUEST),
    CANNOT_DELETE_SUPERUSER("Cannot delete superuser - please remove superuser status first.", SC_FORBIDDEN),
    PASSWORD_IS_EXPIRED("""
            Your password has expired. Please ask your administrator for a new password
            """, SC_UNAUTHORIZED),
    LOCK_FAILED("Failed to lock the given list of objects, see detailed error list for more information", SC_FORBIDDEN),
    UNLOCK_FAILED("Failed to unlock the given list of objects, see detailed error list for more information", SC_FORBIDDEN),
    UI_LANGUAGE_NOT_FOUND("UI language was not found in the database", HttpServletResponse.SC_NOT_FOUND),
    SYSTEM_FOLDER_NOT_FOUND("Folder /system/users was not found in the database", HttpServletResponse.SC_NOT_FOUND),
    DEFAULT_ACL_NOT_FOUND("Could not find the default ACL (with name: "+ Constants.ACL_DEFAULT+")", SC_NOT_FOUND),
    DEFAULT_FOLDER_TYPE_NOT_FOUND("Could not find the default folder type (with name: "+ Constants.FOLDER_TYPE_DEFAULT+")", SC_NOT_FOUND),
    NEED_EXTERNAL_LOGGING_CONFIG("Please configure Cinnamon Server which external log4j2.xml file to use when reconfiguring logging.",SC_BAD_REQUEST ),
    COMMIT_TO_DATABASE_FAILED("Something went wrong when the server tried to save your changes to the database. Your changes may not have been saved. Please contact your system administrator. ",SC_INTERNAL_SERVER_ERROR );

    private static final Map<String, ErrorCode> codeMapping = new ConcurrentHashMap<>();
    final String description;
    final int httpResponseCode;
    final Supplier<FailedRequestException> exceptionSupplier;

    ErrorCode(String description, int httpResponseCode) {
        this.description = description;
        this.httpResponseCode = httpResponseCode;
        this.exceptionSupplier = () -> new FailedRequestException(this);
    }

    public String getCode() {
        return name();
    }

    static {
        for (ErrorCode errorCode : values()) {
            codeMapping.put(errorCode.getCode(), errorCode);
        }
    }

    public String getDescription() {
        return description;
    }

    public static ErrorCode getErrorCode(String code) {
        return codeMapping.getOrDefault(code, UNKNOWN_ERROR_TYPE);
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
