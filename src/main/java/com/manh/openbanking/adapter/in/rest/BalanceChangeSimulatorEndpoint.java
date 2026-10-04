package com.manh.openbanking.adapter.in.rest;

import com.manh.openbanking.application.command.BalanceChangeSubmission;
import com.manh.openbanking.application.port.in.BalanceChangeSimulatorUseCase;
import com.manh.openbanking.application.port.in.BalanceChangeSimulatorUseCase.AcceptedBalanceEvent;
import com.manh.openbanking.domain.event.BalanceChangedEvent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/internal/simulator")
public class BalanceChangeSimulatorEndpoint {
    private final BalanceChangeSimulatorUseCase useCase;

    public BalanceChangeSimulatorEndpoint(BalanceChangeSimulatorUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping(value = "/balance-changes", consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Simulate a balance change and publish the event to Kafka")
    @ApiResponse(responseCode = "202", description = "Balance change event published to Kafka")
    public ResponseEntity<AcceptedBalanceEvent> publish(
        @RequestHeader("Authorization")
        @Pattern(regexp = "Bearer\\s+\\S+", message = "Authorization must use Bearer token format")
        String authorization,
        @RequestHeader("TPP-ID") @NotBlank @Size(max = 15) String tppId,
        @RequestHeader("Provider-ID") @NotBlank @Size(max = 8) String providerId,
        @RequestHeader("Request-ID") @NotBlank @Size(max = 60) String requestId,
        @RequestHeader("Request-DateTime")
        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(?:\\.\\d+)?Z$",
            message = "Request-DateTime must be UTC RFC 3339")
        String requestDateTime,
        @RequestHeader("JWS-Signature") @NotBlank String jwsSignature,
        @Valid @RequestBody BalanceChangeRequest request) {
        String eventId = request.eventId() == null || request.eventId().isBlank()
            ? UUID.randomUUID().toString()
            : request.eventId();
        BalanceChangedEvent event = new BalanceChangedEvent(eventId, request.accountId(), request.transactionId(),
            request.amount(), request.currency(), request.direction(), request.balanceAfter(), request.occurredAt());
        AcceptedBalanceEvent accepted = useCase.publish(new BalanceChangeSubmission(event, authorization, requestId,
            requestDateTime, providerId, tppId, jwsSignature));
        return ResponseEntity.accepted()
            .location(URI.create("/internal/simulator/balance-changes/" + eventId))
            .body(accepted);
    }
}
