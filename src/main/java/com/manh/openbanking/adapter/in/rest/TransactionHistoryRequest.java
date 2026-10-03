package com.manh.openbanking.adapter.in.rest;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public record TransactionHistoryRequest(
    @NotBlank @Size(max = 34) String accountId,
    @NotNull OffsetDateTime fromDate,
    @NotNull OffsetDateTime toDate,
    @Min(1) Integer page,
    @Min(1) Integer size) {

    int effectivePage() {
        return page == null ? 1 : page;
    }

    Integer effectiveSize() {
        return size;
    }
}
