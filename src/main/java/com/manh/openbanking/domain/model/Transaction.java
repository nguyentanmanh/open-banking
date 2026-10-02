package com.manh.openbanking.domain.model;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record Transaction(String transactionId, OffsetDateTime bookingDateTime, LocalDate valueDate,
        TransactionType type, Money amount, String description, String reference,
        Money balanceAfterTransaction) {
    public enum TransactionType { CREDIT, DEBIT }
}
