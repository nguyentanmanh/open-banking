package com.manh.openbanking.application.exception;

import com.manh.openbanking.domain.exception.ErrorCode;

public class ApplicationException extends RuntimeException {
    private final ErrorCode errorCode;

    public ApplicationException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.code(), cause);
        this.errorCode = errorCode;
    }

    public ErrorCode errorCode() {
        return errorCode;
    }
}
