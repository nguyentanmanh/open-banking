package com.manh.openbanking.domain.event;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record BalanceChangedEvent(String eventId, String accountId, String transactionId, BigDecimal amount,
                                  String currency, Direction direction, BigDecimal balanceAfter,
                                  OffsetDateTime occurredAt) {
    public enum Direction {CREDIT, DEBIT}
}
