package com.dewarim.cinnamon.application;

public enum ErrorCode {
    
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
