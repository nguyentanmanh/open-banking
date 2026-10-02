package com.manh.openbanking.adapter.out.mock;

import com.manh.openbanking.application.exception.OpenBankingException;
import com.manh.openbanking.application.port.out.TransactionHistoryProvider;
import com.manh.openbanking.application.query.TransactionHistoryQuery;
import com.manh.openbanking.domain.model.Money;
import com.manh.openbanking.domain.model.Pagination;
import com.manh.openbanking.domain.model.Transaction;
import com.manh.openbanking.domain.model.Transaction.TransactionType;
import com.manh.openbanking.domain.model.TransactionHistory;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

public final class MockTransactionHistoryAdapter implements TransactionHistoryProvider {
    @Override
    public TransactionHistory fetch(TransactionHistoryQuery query) {
        if ("ACC-404".equals(query.accountId())) {
            throw new OpenBankingException("ACCOUNT_NOT_FOUND", "The requested account was not found.", 404);
        }
        if ("ACC-500".equals(query.accountId())) {
            throw new OpenBankingException("INTERNAL_ERROR", "An unexpected internal error occurred.", 500);
        }
        List<Transaction> transactions = List.of(
                new Transaction("TXN-ACC-001-001", OffsetDateTime.of(2026, 9, 30, 3, 45, 12, 0, ZoneOffset.UTC),
                        LocalDate.of(2026, 9, 30), TransactionType.DEBIT, new Money("275000.00", "VND"),
                        "Card purchase at ABC Mart", "POS-845392", new Money("18425000.00", "VND")),
                new Transaction("TXN-ACC-001-002", OffsetDateTime.of(2026, 9, 29, 8, 15, 0, 0, ZoneOffset.UTC),
                        LocalDate.of(2026, 9, 29), TransactionType.CREDIT, new Money("1250000.00", "VND"),
                        "Synthetic salary adjustment", "POC-CREDIT-001", new Money("18700000.00", "VND")));
        int totalPages = transactions.isEmpty() ? 0 : 1;
        return new TransactionHistory(query.accountId(), transactions,
                new Pagination(query.page(), query.pageSize(), transactions.size(), totalPages));
    }
}
