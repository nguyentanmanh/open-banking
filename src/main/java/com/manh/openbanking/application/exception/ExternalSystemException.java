package com.manh.openbanking.application.exception;

import com.manh.openbanking.domain.exception.ErrorCode;

public class ExternalSystemException extends ApplicationException {
    public ExternalSystemException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
