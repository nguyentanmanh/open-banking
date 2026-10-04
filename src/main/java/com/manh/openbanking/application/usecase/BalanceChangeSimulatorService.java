package com.manh.openbanking.application.usecase;

import com.manh.openbanking.application.command.BalanceChangeSubmission;
import com.manh.openbanking.application.port.in.BalanceChangeSimulatorUseCase;
import com.manh.openbanking.application.port.out.BalanceEventPublisher;

public class BalanceChangeSimulatorService implements BalanceChangeSimulatorUseCase {
    private final BalanceEventPublisher publisher;

    public BalanceChangeSimulatorService(BalanceEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public AcceptedBalanceEvent publish(BalanceChangeSubmission submission) {
        publisher.publish(submission);
        return new AcceptedBalanceEvent(submission.event().eventId(), "FORWARDED");
    }
}
