package com.manh.openbanking.adapter.out.messaging;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("open-banking.kafka.balance-events")
public record KafkaBalanceEventProperties(String topic, Duration publishTimeout) {
}
