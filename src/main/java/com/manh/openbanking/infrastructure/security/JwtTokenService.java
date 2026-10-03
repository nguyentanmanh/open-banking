package com.manh.openbanking.infrastructure.security;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {
    private static final Set<String> SUPPORTED_SCOPES = Set.of("transactions:read");

    private final JwtEncoder encoder;
    private final JwtProperties properties;

    public JwtTokenService(JwtEncoder encoder, JwtProperties properties) {
        this.encoder = encoder;
        this.properties = properties;
    }

    public IssuedToken issue(String clientId, String requestedScope, TokenScenario scenario) {
        if (scenario != TokenScenario.VALID && !properties.testScenariosEnabled()) {
            throw new IllegalArgumentException("JWT test scenarios are disabled");
        }
        String scope = scenario == TokenScenario.MISSING_SCOPE ? "" : normalizeScope(requestedScope);
        Instant now = Instant.now();
        Instant issuedAt = scenario == TokenScenario.EXPIRED ? now.minusSeconds(600) : now;
        Instant notBefore = issuedAt;
        Instant expiresAt = scenario == TokenScenario.EXPIRED
            ? now.minusSeconds(300)
            : now.plus(properties.tokenTtl());
        String issuer = scenario == TokenScenario.WRONG_ISSUER
            ? properties.issuer() + "/invalid"
            : properties.issuer();
        String audience = scenario == TokenScenario.WRONG_AUDIENCE
            ? properties.audience() + "-invalid"
            : properties.audience();

        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer(issuer)
            .subject(clientId)
            .audience(List.of(audience))
            .issuedAt(issuedAt)
            .notBefore(notBefore)
            .expiresAt(expiresAt)
            .claim("client_id", clientId)
            .claim("scope", scope)
            .build();
        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).build();
        String value = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new IssuedToken(value, Math.max(0, expiresAt.getEpochSecond() - now.getEpochSecond()), scope);
    }

    private String normalizeScope(String requestedScope) {
        if (requestedScope == null || requestedScope.isBlank()) {
            return "transactions:read";
        }
        List<String> requested = List.of(requestedScope.trim().split("\\s+"));
        if (!SUPPORTED_SCOPES.containsAll(requested)) {
            throw new IllegalArgumentException("Unsupported scope");
        }
        return String.join(" ", requested);
    }

    public enum TokenScenario {
        VALID,
        EXPIRED,
        WRONG_ISSUER,
        WRONG_AUDIENCE,
        MISSING_SCOPE;

        public static TokenScenario from(String value) {
            if (value == null || value.isBlank()) {
                return VALID;
            }
            return valueOf(value.trim().toUpperCase().replace('-', '_'));
        }
    }

    public record IssuedToken(String accessToken, long expiresIn, String scope) {
    }
}
