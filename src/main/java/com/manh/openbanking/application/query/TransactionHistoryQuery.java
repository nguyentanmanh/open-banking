package com.manh.openbanking.application.query;

import java.time.OffsetDateTime;

public record TransactionHistoryQuery(String accountId, OffsetDateTime fromDate,
                                      OffsetDateTime toDate, int page, Integer size) {
}
