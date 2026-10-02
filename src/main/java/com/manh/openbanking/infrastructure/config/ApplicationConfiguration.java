package com.manh.openbanking.infrastructure.config;

import com.manh.openbanking.adapter.out.mock.MockTransactionHistoryAdapter;
import com.manh.openbanking.application.port.in.TransactionHistoryUseCase;
import com.manh.openbanking.application.port.out.TransactionHistoryProvider;
import com.manh.openbanking.application.usecase.TransactionHistoryService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration {
    @Bean TransactionHistoryProvider transactionHistoryProvider() { return new MockTransactionHistoryAdapter(); }
    @Bean TransactionHistoryUseCase transactionHistoryUseCase(TransactionHistoryProvider provider) {
        return new TransactionHistoryService(provider);
    }
}
