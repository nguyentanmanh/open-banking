package com.manh.openbanking.adapter.in.rest;

import com.manh.openbanking.application.port.in.TransactionHistoryUseCase;
import com.manh.openbanking.application.query.TransactionHistoryQuery;
import com.manh.openbanking.domain.model.TransactionHistory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import org.springframework.http.MediaType;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/open-banking/v1/accounts")
public class TransactionHistoryController {
    private final TransactionHistoryUseCase useCase;

    public TransactionHistoryController(TransactionHistoryUseCase useCase) { this.useCase = useCase; }

    @PostMapping(value = "/transactions", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public TransactionHistory getTransactionHistory(
            @RequestHeader("X-Api-Key") @NotBlank String apiKey,
            @RequestHeader("TPP-ID") @NotBlank String tppId,
            @RequestHeader("Provider-ID") @NotBlank String providerId,
            @RequestHeader(value = "Request-ID", required = false) @Size(max = 128) String requestId,
            @RequestHeader("Request-Datetime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    OffsetDateTime requestDateTime,
            @RequestHeader("JWS-Signature") @NotBlank String jwsSignature,
            @Valid @RequestBody TransactionHistoryRequest request) {
        return useCase.getTransactionHistory(new TransactionHistoryQuery(request.consentId(), request.accountId(),
                request.fromDate(), request.toDate(), request.effectivePage(), request.effectivePageSize()));
    }
}
