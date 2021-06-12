package com.dewarim.cinnamon;

import com.dewarim.cinnamon.model.response.CinnamonErrorWrapper;

public class CinnamonClientException extends RuntimeException {

    private ErrorCode            errorCode = ErrorCode.UNKNOWN_ERROR_TYPE;
    private CinnamonErrorWrapper errorWrapper;

    public CinnamonClientException(String message) {
        super(message);
    }

    public CinnamonClientException(ErrorCode errorCode) {
        super(errorCode.getCode());
        this.errorCode = errorCode;
    }

    public CinnamonClientException(CinnamonErrorWrapper wrapper) {
        super(wrapper.getErrors().get(0).getCode());
        this.errorCode = ErrorCode.getErrorCode(wrapper.getErrors().get(0).getCode());
        this.errorWrapper = wrapper;
    }

    public CinnamonClientException(ErrorCode errorCode, CinnamonErrorWrapper wrapper) {
        super(errorCode.getCode());
        this.errorCode = errorCode;
        this.errorWrapper = wrapper;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public CinnamonErrorWrapper getErrorWrapper() {
        return errorWrapper;
    }
}
