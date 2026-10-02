package com.manh.openbanking.application.port.in;

import com.manh.openbanking.application.query.TransactionHistoryQuery;
import com.manh.openbanking.domain.model.TransactionHistory;

public interface TransactionHistoryUseCase {
    TransactionHistory getTransactionHistory(TransactionHistoryQuery query);
}
