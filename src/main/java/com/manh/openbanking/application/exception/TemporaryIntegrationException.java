package com.manh.openbanking.application.exception;

import com.manh.openbanking.domain.exception.CommonErrorCode;

public final class TemporaryIntegrationException extends ExternalSystemException {
    public TemporaryIntegrationException(Throwable cause) { super(CommonErrorCode.EXTERNAL_UNAVAILABLE, cause); }
}
