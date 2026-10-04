package com.manh.openbanking.adapter.out.messaging;

import com.manh.openbanking.domain.event.BalanceChangedEvent.Direction;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record BalanceChangedMessage(String eventType, String eventId, String correlationId,
                                    OffsetDateTime occurredAt, Data data) {
    public record Data(String accountId, String transactionId, BigDecimal amount, String currency,
                       Direction direction, BigDecimal balanceAfter) {
    }
}
