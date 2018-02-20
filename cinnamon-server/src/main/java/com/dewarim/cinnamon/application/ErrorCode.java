package com.dewarim.cinnamon.application;

public enum ErrorCode {
    
    AUTHENTICATION_FAIL_NO_TICKET_GIVEN("error.empty.or.null.ticket.given"),
    AUTHENTICATION_FAIL_NO_SESSION_FOUND("error.no.session.found"),
    AUTHENTICATION_FAIL_SESSION_EXPIRED("error.session.expired"),
    AUTHENTICATION_FAIL_USER_NOT_FOUND("error.user.not.found.or.inactive"),
    USER_INFO_REQUEST_WITHOUT_NAME_OR_ID("error.userInfoRequest.missing.id.or.name"),
    USER_ACCOUNT_NOT_FOUND("error.userInfoRequest.invalid.id.or.name")
    ;
    
    String code;

    ErrorCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
