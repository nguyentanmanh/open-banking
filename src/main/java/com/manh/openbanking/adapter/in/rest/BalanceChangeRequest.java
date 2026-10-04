package com.manh.openbanking.adapter.in.rest;

import com.manh.openbanking.domain.event.BalanceChangedEvent.Direction;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record BalanceChangeRequest(
    String eventId,
    @NotBlank @Size(max = 34) String accountId,
    @NotBlank @Size(max = 70) String transactionId,
    @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal amount,
    @NotBlank @Pattern(regexp = "^[A-Z]{3}$") String currency,
    @NotNull Direction direction,
    @NotNull BigDecimal balanceAfter,
    @NotNull OffsetDateTime occurredAt) {
}
