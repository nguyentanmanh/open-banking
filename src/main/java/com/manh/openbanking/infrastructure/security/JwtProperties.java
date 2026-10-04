package com.manh.openbanking.infrastructure.security;

import java.time.Duration;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("open-banking.jwt")
public record JwtProperties(String issuer, String audience, String privateKeyBase64, Duration tokenTtl,
                            boolean testScenariosEnabled, List<Client> clients) {

    public record Client(String clientId, String clientSecret, List<String> scopes) {
    }
}
