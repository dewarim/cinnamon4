package com.dewarim.cinnamon;

public class CinnamonClientException extends RuntimeException{

    private ErrorCode errorCode = ErrorCode.UNKNOWN_ERROR_TYPE;

    public CinnamonClientException(String message) {
        super(message);
    }

    public CinnamonClientException(ErrorCode errorCode){
        super(errorCode.getCode());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
