package com.manh.openbanking.adapter.out.messaging;

import com.manh.openbanking.application.command.BalanceChangeSubmission;
import com.manh.openbanking.application.exception.OpenBankingException;
import com.manh.openbanking.application.port.out.BalanceEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.concurrent.TimeUnit;

public class KafkaBalanceEventPublisher implements BalanceEventPublisher {
    private static final Logger LOG = LoggerFactory.getLogger(KafkaBalanceEventPublisher.class);

    private final KafkaTemplate<String, BalanceChangedMessage> kafkaTemplate;
    private final KafkaBalanceEventProperties properties;

    public KafkaBalanceEventPublisher(KafkaTemplate<String, BalanceChangedMessage> kafkaTemplate,
                                      KafkaBalanceEventProperties properties) {
        this.kafkaTemplate = kafkaTemplate;
        this.properties = properties;
    }

    @Override
    public void publish(BalanceChangeSubmission submission) {
        var event = submission.event();
        var message = new BalanceChangedMessage(
            "balance.changed",
            event.eventId(),
            submission.requestId(),
            event.occurredAt(),
            new BalanceChangedMessage.Data(event.accountId(), event.transactionId(), event.amount(),
                event.currency(), event.direction(), event.balanceAfter()));
        try {
            var result = kafkaTemplate.send(properties.topic(), accountPartitionKey(event.accountId()), message)
                .get(properties.publishTimeout().toMillis(), TimeUnit.MILLISECONDS);
            LOG.info("balance_event_kafka_published eventId={} topic={} partition={} offset={}",
                event.eventId(), result.getRecordMetadata().topic(), result.getRecordMetadata().partition(),
                result.getRecordMetadata().offset());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw unavailable(exception);
        } catch (Exception exception) {
            throw unavailable(exception);
        }
    }

    private OpenBankingException unavailable(Exception cause) {
        LOG.error("Balance event Kafka publish failed", cause);
        return new OpenBankingException("BALANCE_EVENT_BROKER_UNAVAILABLE",
            "Balance event broker is unavailable", 502);
    }

    private String accountPartitionKey(String accountId) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(accountId.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
