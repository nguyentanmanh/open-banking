package com.manh.openbanking.application.exception;

import com.manh.openbanking.domain.exception.CommonErrorCode;

public final class AccessDeniedException extends ApplicationException {
    public AccessDeniedException() {
        super(CommonErrorCode.ACCESS_DENIED, null);
    }
}
