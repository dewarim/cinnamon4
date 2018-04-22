package com.dewarim.cinnamon.application;

public enum ErrorCode {
    
    AUTHENTICATION_FAIL_NO_TICKET_GIVEN("error.empty.or.null.ticket.given"),
    AUTHENTICATION_FAIL_NO_SESSION_FOUND("error.no.session.found"),
    AUTHENTICATION_FAIL_SESSION_EXPIRED("error.session.expired"),
    AUTHENTICATION_FAIL_USER_NOT_FOUND("error.user.not.found.or.inactive"),
    CONNECTION_FAIL_ACCOUNT_INACTIVE("error.account.inactive"),
    CONNECTION_FAIL_INVALID_USERNAME("error.invalid.username"),
    CONNECTION_FAIL_ACCOUNT_LOCKED("error.account.locked"),
    CONNECTION_FAIL_WRONG_PASSWORD("error.wrong.password"),
    INTERNAL_SERVER_ERROR_TRY_AGAIN_LATER("error.internal.server.error.please.retry.later"),
    USER_INFO_REQUEST_WITHOUT_NAME_OR_ID("error.userInfoRequest.missing.id.or.name"),
    USER_ACCOUNT_NOT_FOUND("error.userInfoRequest.invalid.id.or.name"),
    LOGIN_FAILED("error.login.failed"),
    REQUIRES_SUPERUSER_STATUS("error.action.requires.superuser.status"),
    NAME_PARAM_IS_INVALID("error.name.param.is.invalid"),
    INFO_REQUEST_WITHOUT_NAME_OR_ID("error.request.needs.id.or.name.parameter"),
    DELETE_REQUEST_WITHOUT_ID("error.delete.request.needs.id.parameter"),
    DB_UPDATE_FAILED("error.db.update.failed"),
    ID_PARAM_IS_INVALID("error.id.param.is.missing.or.invalid"),
    OBJECT_NOT_FOUND("error.object.not.found"),
    PERMISSIONS_NOT_FOUND("error.no.permissions.found.for.user.and.acl"),
    UNAUTHORIZED("error.access.not.allowed"), 
    INVALID_REQUEST("error.request.is.invalid.check.parameters"),
    PARENT_FOLDER_NOT_FOUND("error.parent.folder.not.found"), 
    ACL_NOT_FOUND("error.acl.not.found"),
    SESSION_NOT_FOUND("error.session.not.found");
    
    String code;

    ErrorCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
