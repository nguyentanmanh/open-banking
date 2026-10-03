package com.manh.openbanking.application.exception;

import com.manh.openbanking.domain.exception.CommonErrorCode;

public final class UnauthorizedException extends ApplicationException {
    public UnauthorizedException() {
        super(CommonErrorCode.UNAUTHORIZED, null);
    }
}
