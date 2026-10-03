package com.manh.openbanking.domain.exception;

public class BusinessRuleViolationException extends DomainException {
    public BusinessRuleViolationException(ErrorCode errorCode) {
        super(errorCode);
    }
}
