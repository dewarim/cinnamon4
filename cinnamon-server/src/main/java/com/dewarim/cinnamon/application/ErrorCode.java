package com.dewarim.cinnamon.application;

public enum ErrorCode {
    
    AUTHENTICATION_FAIL_NO_TICKET_GIVEN("error.empty.or.null.ticket.given"),
    AUTHENTICATION_FAIL_NO_SESSION_FOUND("error.no.session.found"),
    AUTHENTICATION_FAIL_SESSION_EXPIRED("error.session.expired"),
    AUTHENTICATION_FAIL_USER_NOT_FOUND("error.user.not.found.or.inactive"),
    CONNECTION_FAIL_INVALID_USERNAME("error.invalid.username"),
    CONNECTION_FAIL_WRONG_PASSWORD("error.wrong.password"),
    INTERNAL_SERVER_ERROR_TRY_AGAIN_LATER("error.internal.server.error.please.retry.later"),
    USER_INFO_REQUEST_WITHOUT_NAME_OR_ID("error.userInfoRequest.missing.id.or.name"),
    USER_ACCOUNT_NOT_FOUND("error.userInfoRequest.invalid.id.or.name"),
    LOGIN_FAILED("error.login.failed"), 
    REQUIRES_SUPERUSER_STATUS("error.action.requires.superuser.status"), 
    NAME_PARAM_IS_INVALID("error.name.param.is.invalid");
    
    String code;

    ErrorCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
