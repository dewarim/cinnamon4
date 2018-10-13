package com.dewarim.cinnamon.application.exception;

import com.dewarim.cinnamon.application.ErrorCode;

public class FailedRequestException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String    message;

    public FailedRequestException(ErrorCode errorCode) {
        super();
        this.errorCode = errorCode;
        this.message = errorCode.getCode();
    }

    public FailedRequestException(ErrorCode errorCode, String message) {
        super();
        this.errorCode = errorCode;
        this.message = message;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
