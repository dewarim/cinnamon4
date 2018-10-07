package com.dewarim.cinnamon.application.exception;

import com.dewarim.cinnamon.application.ErrorCode;

public class FailedRequestException extends RuntimeException{

    private final ErrorCode errorCode;
    public FailedRequestException(ErrorCode errorCode) {
        super();
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
