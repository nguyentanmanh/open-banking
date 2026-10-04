package com.manh.openbanking.adapter.out.client;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("open-banking.mule.balance-events")
public record MuleBalanceEventProperties(String baseUrl, String path, String apiKey, Duration connectTimeout,
                                         Duration readTimeout) {
}
