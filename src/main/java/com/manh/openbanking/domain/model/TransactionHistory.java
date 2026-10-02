package com.manh.openbanking.domain.model;

import java.util.List;

public record TransactionHistory(String accountId, List<Transaction> transactions, Pagination pagination) {
    public TransactionHistory { transactions = List.copyOf(transactions); }
}
