package com.manh.openbanking.adapter.out.client;

import com.manh.openbanking.application.command.BalanceChangeSubmission;
import com.manh.openbanking.application.exception.OpenBankingException;
import com.manh.openbanking.application.port.out.BalanceEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.LinkedHashMap;
import java.util.Map;

public class MuleBalanceEventPublisher implements BalanceEventPublisher {
    private final RestClient client;
    private final MuleBalanceEventProperties properties;

    public MuleBalanceEventPublisher(RestClient client, MuleBalanceEventProperties properties) {
        this.client = client;
        this.properties = properties;
    }

    @Override
    public void publish(BalanceChangeSubmission submission) {
        var event = submission.event();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("eventId", event.eventId());
        body.put("accountId", event.accountId());
        body.put("transactionId", event.transactionId());
        body.put("amount", event.amount());
        body.put("currency", event.currency());
        body.put("direction", event.direction());
        body.put("balanceAfter", event.balanceAfter());
        body.put("occurredAt", event.occurredAt());
        try {
            client.post()
                .uri(properties.path())
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, submission.authorization())
                .header("X-API-Key", properties.apiKey())
                .header("Request-ID", submission.requestId())
                .header("Request-DateTime", submission.requestDateTime())
                .header("Provider-ID", submission.providerId())
                .header("TPP-ID", submission.tppId())
                .header("JWS-Signature", submission.jwsSignature())
                .body(body)
                .retrieve()
                .toBodilessEntity();
        } catch (RestClientResponseException exception) {
            throw new OpenBankingException("BALANCE_EVENT_DELIVERY_FAILED",
                "Mule rejected the balance change event with status " + exception.getStatusCode().value(), 502);
        } catch (ResourceAccessException exception) {
            throw new OpenBankingException("BALANCE_EVENT_GATEWAY_UNAVAILABLE",
                "Mule balance event gateway is unavailable", 502);
        }
    }
}
