package com.manh.openbanking.application.exception;

public final class OpenBankingException extends RuntimeException {
    private final String code;
    private final int status;

    public OpenBankingException(String code, String description, int status) {
        super(description);
        this.code = code;
        this.status = status;
    }

    public String code() { return code; }
    public int status() { return status; }
}
