package com.manh.openbanking.domain.model;

import java.time.OffsetDateTime;

public record Transaction(Money amount, Money balances, CreditDebitIndicator creditDebitIndicator,
                          Boolean reversalIndicator, OffsetDateTime valueDate, References references,
                          RelatedParties relatedParties, String additionalTransactionInformation,
                          Object additionalInfo) {
    public enum CreditDebitIndicator {CRDT, DBIT}

    public record References(String instructionIdentification) {
    }

    public record RelatedParties(Party debtor, Party creditor) {
    }

    public record Party(String name, String bankCode, String accountId) {
    }
}
