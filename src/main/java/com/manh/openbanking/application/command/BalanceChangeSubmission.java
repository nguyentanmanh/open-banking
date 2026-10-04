package com.manh.openbanking.application.command;

import com.manh.openbanking.domain.event.BalanceChangedEvent;

public record BalanceChangeSubmission(BalanceChangedEvent event, String authorization, String requestId,
                                      String requestDateTime, String providerId, String tppId,
                                      String jwsSignature) {
}
