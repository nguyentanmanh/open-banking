package com.manh.openbanking.application.port.out;

import com.manh.openbanking.application.command.BalanceChangeSubmission;

public interface BalanceEventPublisher {
    void publish(BalanceChangeSubmission submission);
}
