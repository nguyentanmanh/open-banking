package com.manh.openbanking.adapter.in.rest;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.manh.openbanking.bootstrap.Application;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
class TransactionHistoryApiTest {
    @Autowired MockMvc mvc;

    @Test
    void returnsContractShapedTransactionHistory() throws Exception {
        mvc.perform(validRequest("ACC-001").header("Request-ID", "req-poc-001"))
                .andExpect(status().isOk())
                .andExpect(header().string("Request-ID", "req-poc-001"))
                .andExpect(jsonPath("$.accountId").value("ACC-001"))
                .andExpect(jsonPath("$.transactions[0].transactionId").exists())
                .andExpect(jsonPath("$.transactions[0].bookingDateTime").exists())
                .andExpect(jsonPath("$.transactions[0].type").value("DEBIT"))
                .andExpect(jsonPath("$.transactions[0].amount.amount").value("275000.00"))
                .andExpect(jsonPath("$.transactions[0].amount.currency").value("VND"))
                .andExpect(jsonPath("$.transactions[0].description").exists())
                .andExpect(jsonPath("$.pagination.page").value(1))
                .andExpect(jsonPath("$.pagination.pageSize").value(50))
                .andExpect(jsonPath("$.pagination.totalRecords").value(2))
                .andExpect(jsonPath("$.pagination.totalPages").value(1));
    }

    @Test
    void generatesRequestIdWhenMissing() throws Exception {
        mvc.perform(validRequest("ACC-001"))
                .andExpect(status().isOk())
                .andExpect(header().string("Request-ID", matchesPattern(
                        "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")));
    }

    @Test
    void returnsContractErrorForMissingAccount() throws Exception {
        mvc.perform(validRequest("ACC-404").header("Request-ID", "req-404"))
                .andExpect(status().isNotFound())
                .andExpect(header().string("Request-ID", "req-404"))
                .andExpect(jsonPath("$.code").value("ACCOUNT_NOT_FOUND"))
                .andExpect(jsonPath("$.description").exists())
                .andExpect(jsonPath("$.message").doesNotExist());
    }

    @Test
    void returnsContractErrorForSystemFailure() throws Exception {
        mvc.perform(validRequest("ACC-500").header("Request-ID", "req-500"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.description").exists());
    }

    @Test
    void rejectsInvalidDateRange() throws Exception {
        mvc.perform(contractHeaders(post("/open-banking/v1/accounts/transactions"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"consentId":"CNS-001","accountId":"ACC-001","fromDate":"2026-10-02","toDate":"2026-10-01"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.description").value("fromDate must be on or before toDate"));
    }

    @Test
    void contextStarts() { }

    private MockHttpServletRequestBuilder validRequest(String accountId) {
        return contractHeaders(post("/open-banking/v1/accounts/transactions"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"consentId":"CNS-001","accountId":"%s","fromDate":"2026-09-01","toDate":"2026-09-30"}
                        """.formatted(accountId));
    }

    private MockHttpServletRequestBuilder contractHeaders(MockHttpServletRequestBuilder request) {
        return request.header("X-Api-Key", "poc-key")
                .header("TPP-ID", "TPP-00042")
                .header("Provider-ID", "BANK-VN-01")
                .header("Request-Datetime", "2026-10-01T08:29:54Z")
                .header("JWS-Signature", "poc-not-verified");
    }
}
