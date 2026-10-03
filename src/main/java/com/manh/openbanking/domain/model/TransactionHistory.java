package com.manh.openbanking.domain.model;

import java.util.List;

public record TransactionHistory(int pageCount, int pageNumber, Integer nextPage, int pageSize,
                                 Long totalCount, List<Transaction> transactions) {
    public TransactionHistory {
        transactions = List.copyOf(transactions);
    }
}
