package com.manh.openbanking.infrastructure.security;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("open-banking.jwt")
public record JwtProperties(String issuer, String audience, String clientId, String clientSecret,
                               String privateKeyBase64, Duration tokenTtl, boolean testScenariosEnabled) {
}
