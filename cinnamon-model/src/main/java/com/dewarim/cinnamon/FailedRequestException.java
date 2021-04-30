package com.dewarim.cinnamon;

import com.dewarim.cinnamon.model.response.CinnamonError;

import java.util.List;

/**
 * The FailedRequestException is thrown when an expected problem is detected
 * that should be reported to the user with a specific error code.
 * (for example: missing permission to actually delete an object).
 */
public class FailedRequestException extends RuntimeException {

    private final ErrorCode           errorCode;
    private final String              message;
    private       List<CinnamonError> errors;

    public FailedRequestException(ErrorCode errorCode) {
        super();
        this.errorCode = errorCode;
        this.message = errorCode.getCode();
    }

    public FailedRequestException( ErrorCode errorCode, Throwable cause) {
        super(cause);
        this.errorCode = errorCode;
        this.message=errorCode.getCode();

    }

    public FailedRequestException(ErrorCode errorCode, String message) {
        super();
        this.errorCode = errorCode;
        this.message = message;
    }

    public FailedRequestException(ErrorCode errorCode, List<CinnamonError> errors) {
        this.errorCode = errorCode;
        this.message = errorCode.getCode();
        this.errors = errors;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    @Override
    public String getMessage() {
        return message;
    }


}
