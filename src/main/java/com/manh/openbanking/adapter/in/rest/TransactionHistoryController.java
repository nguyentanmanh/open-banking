package com.manh.openbanking.adapter.in.rest;

import com.manh.openbanking.application.exception.OpenBankingException;
import com.manh.openbanking.application.port.in.TransactionHistoryUseCase;
import com.manh.openbanking.application.query.TransactionHistoryQuery;
import com.manh.openbanking.domain.model.TransactionHistory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneOffset;

@Validated
@RestController
@RequestMapping("/v1/accounts")
public class TransactionHistoryController {
    private final TransactionHistoryUseCase useCase;

    public TransactionHistoryController(TransactionHistoryUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping(value = "/transactions", consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TransactionHistory> getTransactionHistory(
        @RequestHeader("Authorization") @Pattern(regexp = "Bearer\\s+\\S+", message = "Authorization must use Bearer token format") String authorization,
        @RequestHeader("TPP-ID") @NotBlank @Size(max = 15) String tppId,
        @RequestHeader("Provider-ID") @NotBlank @Size(max = 8) String providerId,
        @RequestHeader("Request-ID") @NotBlank @Size(max = 60) String requestId,
        @RequestHeader("Request-DateTime")
        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(?:\\.\\d+)?Z$", message = "Request-DateTime must be UTC RFC 3339")
        String requestDateTime,
        @RequestHeader(value = "PSU-IP-Address", required = false) String psuIpAddress,
        @RequestHeader(value = "PSU-User-Agent", required = false) @Size(max = 200) String psuUserAgent,
        @RequestHeader(value = "PSU-Device-OS", required = false) @Size(max = 100) String psuDeviceOs,
        @RequestHeader(value = "Client-ID", required = false) @Size(max = 50) String clientId,
        @RequestHeader("JWS-Signature") @NotBlank String jwsSignature,
        @Valid @RequestBody TransactionHistoryRequest request) {
        if (!ZoneOffset.UTC.equals(request.fromDate().getOffset()) || request.fromDate().isAfter(request.toDate())) {
            throw new OpenBankingException(
                "FROMDATE_INVALID", "Dữ liệu trường fromDate không hợp lệ", 400);
        }
        if (!ZoneOffset.UTC.equals(request.toDate().getOffset())) {
            throw new OpenBankingException(
                "TODATE_INVALID", "Dữ liệu trường toDate không hợp lệ", 400);
        }
        TransactionHistory response = useCase.getTransactionHistory(new TransactionHistoryQuery(request.accountId(),
            request.fromDate(), request.toDate(), request.effectivePage(), request.effectiveSize()));
        return ResponseEntity.ok()
            .header("Request-ID", requestId)
            .header("Request-DateTime", requestDateTime)
            .header("JWS-Signature", jwsSignature)
            .body(response);
    }
}
