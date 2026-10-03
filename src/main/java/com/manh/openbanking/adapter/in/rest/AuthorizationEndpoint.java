package com.manh.openbanking.adapter.in.rest;

import com.manh.openbanking.infrastructure.security.JwtProperties;
import com.manh.openbanking.infrastructure.security.JwtTokenService;
import com.manh.openbanking.infrastructure.security.JwtTokenService.IssuedToken;
import com.manh.openbanking.infrastructure.security.JwtTokenService.TokenScenario;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;

/**
 * Self-contained authorization boundary for the MuleSoft demonstration environment.
 * Production deployments must use a dedicated, managed authorization server.
 */
@RestController
public class AuthorizationEndpoint {
    private final JwtTokenService tokenService;
    private final JwtProperties properties;
    private final RSAKey rsaKey;

    public AuthorizationEndpoint(JwtTokenService tokenService, JwtProperties properties, RSAKey rsaKey) {
        this.tokenService = tokenService;
        this.properties = properties;
        this.rsaKey = rsaKey;
    }

    @PostMapping(value = "/oauth2/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> token(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                     @RequestParam("grant_type") String grantType,
                                     @RequestParam(value = "scope", required = false) String scope,
                                     @RequestParam(value = "test_case", required = false) String testCase) {
        authenticateClient(authorization);
        if (!"client_credentials".equals(grantType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "unsupported_grant_type");
        }
        try {
            IssuedToken token = tokenService.issue(properties.clientId(), scope, TokenScenario.from(testCase));
            return Map.of(
                "access_token", token.accessToken(),
                "token_type", "Bearer",
                "expires_in", token.expiresIn(),
                "scope", token.scope());
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        }
    }

    @GetMapping(value = "/.well-known/jwks.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> jwks() {
        return ResponseEntity.ok()
            .cacheControl(CacheControl.maxAge(Duration.ofMinutes(1)).cachePublic())
            .body(new JWKSet(rsaKey.toPublicJWK()).toJSONObject());
    }

    private void authenticateClient(String authorization) {
        if (authorization == null || !authorization.startsWith("Basic ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid_client");
        }
        try {
            String credentials = new String(Base64.getDecoder().decode(authorization.substring(6)),
                StandardCharsets.UTF_8);
            int separator = credentials.indexOf(':');
            if (separator < 0
                || !constantTimeEquals(credentials.substring(0, separator), properties.clientId())
                || !constantTimeEquals(credentials.substring(separator + 1), properties.clientSecret())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid_client");
            }
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid_client", exception);
        }
    }

    private boolean constantTimeEquals(String actual, String expected) {
        return MessageDigest.isEqual(actual.getBytes(StandardCharsets.UTF_8), expected.getBytes(StandardCharsets.UTF_8));
    }
}
