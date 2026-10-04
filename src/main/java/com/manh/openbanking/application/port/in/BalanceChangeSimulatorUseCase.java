package com.manh.openbanking.application.port.in;

import com.manh.openbanking.application.command.BalanceChangeSubmission;

public interface BalanceChangeSimulatorUseCase {
    AcceptedBalanceEvent publish(BalanceChangeSubmission submission);

    record AcceptedBalanceEvent(String eventId, String status) {
    }
}
