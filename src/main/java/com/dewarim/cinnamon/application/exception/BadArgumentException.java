package com.dewarim.cinnamon.application.exception;

import com.dewarim.cinnamon.ErrorCode;

public class BadArgumentException extends RuntimeException {

    private final ErrorCode errorCode;

    public BadArgumentException(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
