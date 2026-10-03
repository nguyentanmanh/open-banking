package com.manh.openbanking.application.exception;

import com.manh.openbanking.domain.exception.CommonErrorCode;

public final class ExternalSystemTimeoutException extends ExternalSystemException {
    public ExternalSystemTimeoutException(Throwable cause) {
        super(CommonErrorCode.EXTERNAL_TIMEOUT, cause);
    }
}
