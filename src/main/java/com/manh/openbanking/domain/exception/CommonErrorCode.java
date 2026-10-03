package com.manh.openbanking.domain.exception;

public enum CommonErrorCode implements ErrorCode {
    INVALID_REQUEST("OB-COMMON-001", "error.common.invalid-request"),
    INTERNAL_ERROR("OB-COMMON-002", "error.common.internal"),
    UNAUTHORIZED("OB-AUTH-001", "error.auth.unauthorized"),
    ACCESS_DENIED("OB-AUTH-002", "error.auth.access-denied"),
    EXTERNAL_TIMEOUT("OB-INTEGRATION-001", "error.integration.timeout"),
    EXTERNAL_UNAVAILABLE("OB-INTEGRATION-002", "error.integration.unavailable");
    private final String code;
    private final String messageKey;

    CommonErrorCode(String code, String messageKey) {
        this.code = code;
        this.messageKey = messageKey;
    }

    public String code() {
        return code;
    }

    public String messageKey() {
        return messageKey;
    }
}
