package com.manh.openbanking.adapter.in.rest;

import com.manh.openbanking.application.command.BalanceChangeSubmission;
import com.manh.openbanking.application.port.in.BalanceChangeSimulatorUseCase;
import com.manh.openbanking.bootstrap.Application;
import com.manh.openbanking.infrastructure.security.JwtTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
class BalanceChangeSimulatorApiTest {
    @Autowired
    MockMvc mvc;

    @Autowired
    JwtTokenService tokenService;

    @MockitoBean
    BalanceChangeSimulatorUseCase useCase;

    @Test
    void publishesBalanceChangeEventToKafka() throws Exception {
        when(useCase.publish(any())).thenAnswer(invocation -> {
            BalanceChangeSubmission submission = invocation.getArgument(0);
            return new BalanceChangeSimulatorUseCase.AcceptedBalanceEvent(submission.event().eventId(), "PUBLISHED");
        });
        String token = tokenService.issue("tpp-standard", "transactions:read", Set.of("transactions:read"),
            JwtTokenService.TokenScenario.VALID).accessToken();

        mvc.perform(post("/internal/simulator/balance-changes")
                .header("Authorization", "Bearer " + token)
                .header("TPP-ID", "123456789012345")
                .header("Provider-ID", "BANK0001")
                .header("Request-ID", "req-balance-001")
                .header("Request-DateTime", "2026-10-04T08:30:00Z")
                .header("JWS-Signature", "demo-not-verified")
                .contentType("application/json")
                .content("""
                    {
                      "eventId": "evt-001",
                      "accountId": "1234567890",
                      "transactionId": "txn-001",
                      "amount": 500000,
                      "currency": "VND",
                      "direction": "DEBIT",
                      "balanceAfter": 12500000,
                      "occurredAt": "2026-10-04T05:00:00Z"
                    }
                    """))
            .andExpect(status().isAccepted())
            .andExpect(header().string("Location", "/internal/simulator/balance-changes/evt-001"))
            .andExpect(jsonPath("$.eventId").value("evt-001"))
            .andExpect(jsonPath("$.status").value("PUBLISHED"));

        var captor = org.mockito.ArgumentCaptor.forClass(BalanceChangeSubmission.class);
        verify(useCase).publish(captor.capture());
        assertThat(captor.getValue().event().accountId()).isEqualTo("1234567890");
        assertThat(captor.getValue().authorization()).startsWith("Bearer ");
    }

    @Test
    void publishesSimulatorEndpointInOpenApi() throws Exception {
        mvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.paths['/internal/simulator/balance-changes'].post").exists())
            .andExpect(jsonPath("$.paths['/internal/simulator/balance-changes'].post.responses['202']").exists());
    }
}
