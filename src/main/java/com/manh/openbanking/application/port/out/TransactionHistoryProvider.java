package com.manh.openbanking.application.port.out;

import com.manh.openbanking.application.query.TransactionHistoryQuery;
import com.manh.openbanking.domain.model.TransactionHistory;

public interface TransactionHistoryProvider {
    TransactionHistory fetch(TransactionHistoryQuery query);
}
