package com.dewarim.cinnamon.application.exception;

import com.dewarim.cinnamon.application.ErrorCode;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.application.TransactionStatus;

/**
 * The FailedRequestException is thrown when an expected problem is detected
 * that should be reported to the user with a specific error code.
 * (for example: missing permission to acutally delete an object).
 */
public class FailedRequestException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String    message;

    public FailedRequestException(ErrorCode errorCode) {
        super();
        this.errorCode = errorCode;
        this.message = errorCode.getCode();
        ThreadLocalSqlSession.setTransactionStatus(TransactionStatus.ROLLBACK);
    }

    public FailedRequestException(ErrorCode errorCode, String message) {
        super();
        this.errorCode = errorCode;
        this.message = message;
        ThreadLocalSqlSession.setTransactionStatus(TransactionStatus.ROLLBACK);
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
