package com.manh.openbanking.adapter.in.rest;

import com.manh.openbanking.bootstrap.Application;
import com.manh.openbanking.infrastructure.security.JwtTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
class TransactionHistoryApiTest {
    @Autowired
    MockMvc mvc;

    @Autowired
    JwtTokenService tokenService;

    @Test
    void returnsCircular64TransactionHistoryContract() throws Exception {
        mvc.perform(validRequest("ACC-001"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Type", "application/json"))
            .andExpect(header().string("Request-ID", "req-demo-001"))
            .andExpect(header().string("Request-DateTime", "2026-10-01T08:29:54Z"))
            .andExpect(header().string("JWS-Signature", "demo-detached-jws"))
            .andExpect(jsonPath("$.pageCount").value(1))
            .andExpect(jsonPath("$.pageNumber").value(1))
            .andExpect(jsonPath("$.pageSize").value(2))
            .andExpect(jsonPath("$.totalCount").value(2))
            .andExpect(jsonPath("$.nextPage").doesNotExist())
            .andExpect(jsonPath("$.transactions[0].amount.value").value(275000.00))
            .andExpect(jsonPath("$.transactions[0].amount.currency").value("VND"))
            .andExpect(jsonPath("$.transactions[0].balances.value").value(18425000.00))
            .andExpect(jsonPath("$.transactions[0].creditDebitIndicator").value("DBIT"))
            .andExpect(jsonPath("$.transactions[0].valueDate").value("2026-09-30T03:45:12Z"))
            .andExpect(jsonPath("$.transactions[0].references.instructionIdentification")
                .value("TXN-ACC-001-001"))
            .andExpect(jsonPath("$.transactions[0].additionalTransactionInformation").exists())
            .andExpect(jsonPath("$.accountId").doesNotExist())
            .andExpect(jsonPath("$.pagination").doesNotExist());
    }

    @Test
    void rejectsMissingRequestIdWithCircular64Code() throws Exception {
        mvc.perform(headers(post("/v1/accounts/transactions"), false)
                .contentType(MediaType.APPLICATION_JSON).content(validBody("ACC-001")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("REQUEST_ID_REQUIRED"));
    }

    @Test
    void returnsCircular64CodeForMissingAccount() throws Exception {
        mvc.perform(validRequest("ACC-404"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("ACCOUNT_NOT_EXISTED"))
            .andExpect(jsonPath("$.description").exists())
            .andExpect(jsonPath("$.message").doesNotExist());
    }

    @Test
    void returnsCircular64CodeForSystemFailure() throws Exception {
        mvc.perform(validRequest("ACC-500"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"));
    }

    @Test
    void rejectsInvalidDateRange() throws Exception {
        mvc.perform(headers(post("/v1/accounts/transactions"), true)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"accountId":"ACC-001","fromDate":"2026-10-02T00:00:00Z","toDate":"2026-10-01T00:00:00Z"}
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("FROMDATE_INVALID"));
    }

    @Test
    void rejectsLegacyFieldsBecauseContractIsStrict() throws Exception {
        mvc.perform(headers(post("/v1/accounts/transactions"), true)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"consentId":"CNS-001","accountId":"ACC-001","fromDate":"2026-09-01T00:00:00Z","toDate":"2026-09-30T23:59:59Z"}
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("OTHER"));
    }

    @Test
    void contextStarts() {
    }

    private MockHttpServletRequestBuilder validRequest(String accountId) {
        return headers(post("/v1/accounts/transactions"), true)
            .contentType(MediaType.APPLICATION_JSON).content(validBody(accountId));
    }

    private String validBody(String accountId) {
        return """
            {"accountId":"%s","fromDate":"2026-09-01T00:00:00Z","toDate":"2026-09-30T23:59:59Z"}
            """.formatted(accountId);
    }

    private MockHttpServletRequestBuilder headers(MockHttpServletRequestBuilder request, boolean includeRequestId) {
        String accessToken = tokenService.issue("tpp-standard", "transactions:read", Set.of("transactions:read"),
            JwtTokenService.TokenScenario.VALID).accessToken();
        request.header("Authorization", "Bearer " + accessToken)
            .header("TPP-ID", "123456789012345")
            .header("Provider-ID", "BANK0001")
            .header("Request-DateTime", "2026-10-01T08:29:54Z")
            .header("JWS-Signature", "demo-detached-jws");
        return includeRequestId ? request.header("Request-ID", "req-demo-001") : request;
    }
}
