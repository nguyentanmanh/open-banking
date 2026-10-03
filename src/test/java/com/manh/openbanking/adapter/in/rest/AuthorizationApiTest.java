package com.manh.openbanking.adapter.in.rest;

import com.manh.openbanking.bootstrap.Application;
import com.manh.openbanking.infrastructure.security.JwtTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
class AuthorizationApiTest {
    @Autowired
    MockMvc mvc;

    @Autowired
    JwtDecoder decoder;

    @Autowired
    JwtTokenService tokenService;

    @Test
    void publishesPublicJwk() throws Exception {
        mvc.perform(get("/.well-known/jwks.json"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.keys[0].kty").value("RSA"))
            .andExpect(jsonPath("$.keys[0].alg").value("RS256"))
            .andExpect(jsonPath("$.keys[0].d").doesNotExist());
    }

    @Test
    void issuesValidClientCredentialsToken() throws Exception {
        String response = mvc.perform(post("/oauth2/token")
                .with(httpBasic("tpp-standard", "change-me-before-deploy"))
                .contentType("application/x-www-form-urlencoded")
                .param("grant_type", "client_credentials")
                .param("scope", "transactions:read"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token_type").value("Bearer"))
            .andExpect(jsonPath("$.expires_in").isNumber())
            .andReturn().getResponse().getContentAsString();

        String token = new com.fasterxml.jackson.databind.ObjectMapper().readTree(response).get("access_token").asText();
        var jwt = decoder.decode(token);
        assertThat(jwt.getAudience()).contains("open-banking-api");
        assertThat(jwt.getClaimAsString("scope")).isEqualTo("transactions:read");
    }

    @Test
    void rejectsInvalidClientCredentials() throws Exception {
        mvc.perform(post("/oauth2/token")
                .with(httpBasic("tpp-standard", "wrong-secret"))
                .contentType("application/x-www-form-urlencoded")
                .param("grant_type", "client_credentials"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void issuesExpiredTokenForNegativeTesting() throws Exception {
        String response = mvc.perform(post("/oauth2/token")
                .with(httpBasic("tpp-standard", "change-me-before-deploy"))
                .contentType("application/x-www-form-urlencoded")
                .param("grant_type", "client_credentials")
                .param("test_case", "expired"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        String token = new com.fasterxml.jackson.databind.ObjectMapper().readTree(response).get("access_token").asText();
        assertThatThrownBy(() -> decoder.decode(token)).hasMessageContaining("expired");
    }

    @Test
    void tokenWithoutRequiredScopeCannotAccessTransactions() throws Exception {
        String response = mvc.perform(post("/oauth2/token")
                .with(httpBasic("tpp-standard", "change-me-before-deploy"))
                .contentType("application/x-www-form-urlencoded")
                .param("grant_type", "client_credentials")
                .param("test_case", "missing_scope"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        String token = new com.fasterxml.jackson.databind.ObjectMapper().readTree(response).get("access_token").asText();

        mvc.perform(post("/v1/accounts/transactions")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json"))
            .andExpect(status().isForbidden());
    }

    @Test
    void rejectsWrongIssuerAndAudienceDuringJwtValidation() {
        String wrongIssuer = tokenService.issue("tpp-standard", "transactions:read",
            JwtTokenService.TokenScenario.WRONG_ISSUER).accessToken();
        String wrongAudience = tokenService.issue("tpp-standard", "transactions:read",
            JwtTokenService.TokenScenario.WRONG_AUDIENCE).accessToken();

        assertThatThrownBy(() -> decoder.decode(wrongIssuer)).hasMessageContaining("iss claim");
        assertThatThrownBy(() -> decoder.decode(wrongAudience)).hasMessageContaining("audience");
    }
}
