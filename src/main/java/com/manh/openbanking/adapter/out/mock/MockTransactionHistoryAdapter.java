package com.manh.openbanking.adapter.out.mock;

import com.manh.openbanking.application.exception.OpenBankingException;
import com.manh.openbanking.application.port.out.TransactionHistoryProvider;
import com.manh.openbanking.application.query.TransactionHistoryQuery;
import com.manh.openbanking.domain.model.Money;
import com.manh.openbanking.domain.model.Pagination;
import com.manh.openbanking.domain.model.Transaction;
import com.manh.openbanking.domain.model.Transaction.CreditDebitIndicator;
import com.manh.openbanking.domain.model.Transaction.Party;
import com.manh.openbanking.domain.model.Transaction.References;
import com.manh.openbanking.domain.model.Transaction.RelatedParties;
import com.manh.openbanking.domain.model.TransactionHistory;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

public final class MockTransactionHistoryAdapter implements TransactionHistoryProvider {
    @Override
    public TransactionHistory fetch(TransactionHistoryQuery query) {
        if ("ACC-404".equals(query.accountId())) {
            throw new OpenBankingException("ACCOUNT_NOT_EXISTED", "Tài khoản không tồn tại", 400);
        }
        if ("ACC-500".equals(query.accountId())) {
            throw new OpenBankingException("INTERNAL_ERROR", "An unexpected internal error occurred.", 500);
        }
        List<Transaction> transactions = List.of(
            new Transaction(new Money(new BigDecimal("275000.00"), "VND"),
                new Money(new BigDecimal("18425000.00"), "VND"), CreditDebitIndicator.DBIT, false,
                OffsetDateTime.of(2026, 9, 30, 3, 45, 12, 0, ZoneOffset.UTC),
                new References("TXN-ACC-001-001"),
                new RelatedParties(new Party("NGUYEN VAN A", "BANK0001", "ACC-001"),
                    new Party("ABC MART", "BANK0002", "ACC-MERCHANT-001")),
                "Card purchase at ABC Mart", null),
            new Transaction(new Money(new BigDecimal("1250000.00"), "VND"),
                new Money(new BigDecimal("18700000.00"), "VND"), CreditDebitIndicator.CRDT, false,
                OffsetDateTime.of(2026, 9, 29, 8, 15, 0, 0, ZoneOffset.UTC),
                new References("TXN-ACC-001-002"), null, "Synthetic salary adjustment", null));
        int totalPages = transactions.isEmpty() ? 0 : 1;
        int pageSize = query.size() == null ? transactions.size() : query.size();
        Pagination pagination = new Pagination(totalPages, query.page(), null, pageSize, (long) transactions.size());
        return new TransactionHistory(pagination.pageCount(), pagination.pageNumber(), pagination.nextPage(),
            pagination.pageSize(), pagination.totalCount(), transactions);
    }
}
