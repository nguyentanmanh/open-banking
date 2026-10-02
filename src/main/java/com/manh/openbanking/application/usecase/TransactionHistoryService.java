package com.manh.openbanking.application.usecase;

import com.manh.openbanking.application.port.in.TransactionHistoryUseCase;
import com.manh.openbanking.application.port.out.TransactionHistoryProvider;
import com.manh.openbanking.application.query.TransactionHistoryQuery;
import com.manh.openbanking.domain.model.TransactionHistory;

public final class TransactionHistoryService implements TransactionHistoryUseCase {
    private final TransactionHistoryProvider provider;

    public TransactionHistoryService(TransactionHistoryProvider provider) { this.provider = provider; }

    @Override
    public TransactionHistory getTransactionHistory(TransactionHistoryQuery query) {
        return provider.fetch(query);
    }
}
