package com.dewarim.cinnamon;

import com.dewarim.cinnamon.model.response.CinnamonError;

import java.util.ArrayList;
import java.util.List;

/**
 * The FailedRequestException is thrown when an expected problem is detected
 * that should be reported to the user with a specific error code.
 * (for example: missing permission to actually delete an object).
 */
public class FailedRequestException extends RuntimeException {

    private final ErrorCode           errorCode;
    private       List<CinnamonError> errors = new ArrayList<>();

    public FailedRequestException(ErrorCode errorCode) {
        super(errorCode.getCode());
        this.errorCode = errorCode;
    }

    public FailedRequestException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getCode(),cause);
        this.errorCode = errorCode;

    }

    public FailedRequestException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public FailedRequestException(ErrorCode errorCode, List<CinnamonError> errors) {
        super(errorCode.getCode());
        this.errorCode = errorCode;
        this.errors = errors;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public List<CinnamonError> getErrors() {
        return errors;
    }
}
