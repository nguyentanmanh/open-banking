package com.manh.openbanking.application.query;

import java.time.LocalDate;

public record TransactionHistoryQuery(String consentId, String accountId, LocalDate fromDate,
        LocalDate toDate, int page, int pageSize) { }
