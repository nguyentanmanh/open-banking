package com.manh.openbanking.infrastructure.config;

import com.manh.openbanking.adapter.out.client.MuleBalanceEventProperties;
import com.manh.openbanking.adapter.out.client.MuleBalanceEventPublisher;
import com.manh.openbanking.adapter.out.mock.MockTransactionHistoryAdapter;
import com.manh.openbanking.application.port.in.BalanceChangeSimulatorUseCase;
import com.manh.openbanking.application.port.in.TransactionHistoryUseCase;
import com.manh.openbanking.application.port.out.BalanceEventPublisher;
import com.manh.openbanking.application.port.out.TransactionHistoryProvider;
import com.manh.openbanking.application.usecase.BalanceChangeSimulatorService;
import com.manh.openbanking.application.usecase.TransactionHistoryService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(MuleBalanceEventProperties.class)
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
    RestClient muleBalanceEventRestClient(RestClient.Builder builder, MuleBalanceEventProperties properties) {
        var settings = ClientHttpRequestFactorySettings.defaults()
            .withConnectTimeout(properties.connectTimeout())
            .withReadTimeout(properties.readTimeout());
        return builder
            .baseUrl(properties.baseUrl())
            .requestFactory(ClientHttpRequestFactoryBuilder.detect().build(settings))
            .build();
    }

    @Bean
    BalanceEventPublisher balanceEventPublisher(RestClient muleBalanceEventRestClient,
                                                MuleBalanceEventProperties properties) {
        return new MuleBalanceEventPublisher(muleBalanceEventRestClient, properties);
    }

    @Bean
    BalanceChangeSimulatorUseCase balanceChangeSimulatorUseCase(BalanceEventPublisher publisher) {
        return new BalanceChangeSimulatorService(publisher);
    }
}
