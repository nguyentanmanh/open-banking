package com.manh.openbanking.infrastructure.config;

import com.manh.openbanking.adapter.out.messaging.BalanceChangedMessage;
import com.manh.openbanking.adapter.out.messaging.KafkaBalanceEventProperties;
import com.manh.openbanking.adapter.out.messaging.KafkaBalanceEventPublisher;
import com.manh.openbanking.adapter.out.mock.MockTransactionHistoryAdapter;
import com.manh.openbanking.application.port.in.BalanceChangeSimulatorUseCase;
import com.manh.openbanking.application.port.in.TransactionHistoryUseCase;
import com.manh.openbanking.application.port.out.BalanceEventPublisher;
import com.manh.openbanking.application.port.out.TransactionHistoryProvider;
import com.manh.openbanking.application.usecase.BalanceChangeSimulatorService;
import com.manh.openbanking.application.usecase.TransactionHistoryService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
@EnableConfigurationProperties(KafkaBalanceEventProperties.class)
public class ApplicationConfiguration {
    @Bean
    TransactionHistoryProvider transactionHistoryProvider() {
        return new MockTransactionHistoryAdapter();
    }

    @Bean
    TransactionHistoryUseCase transactionHistoryUseCase(TransactionHistoryProvider provider) {
        return new TransactionHistoryService(provider);
    }

    @Bean
    BalanceEventPublisher balanceEventPublisher(KafkaTemplate<String, BalanceChangedMessage> kafkaTemplate,
                                                KafkaBalanceEventProperties properties) {
        return new KafkaBalanceEventPublisher(kafkaTemplate, properties);
    }

    @Bean
    BalanceChangeSimulatorUseCase balanceChangeSimulatorUseCase(BalanceEventPublisher publisher) {
        return new BalanceChangeSimulatorService(publisher);
    }
}
