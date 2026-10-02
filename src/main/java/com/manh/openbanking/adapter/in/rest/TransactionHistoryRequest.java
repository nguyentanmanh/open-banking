package com.manh.openbanking.adapter.in.rest;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record TransactionHistoryRequest(
        @NotBlank String consentId,
        @NotBlank String accountId,
        @NotNull LocalDate fromDate,
        @NotNull LocalDate toDate,
        @Min(1) Integer page,
        @Min(1) @Max(200) Integer pageSize) {

    @AssertTrue(message = "fromDate must be on or before toDate")
    public boolean isDateRangeValid() {
        return fromDate == null || toDate == null || !fromDate.isAfter(toDate);
    }

    int effectivePage() { return page == null ? 1 : page; }
    int effectivePageSize() { return pageSize == null ? 50 : pageSize; }
}
