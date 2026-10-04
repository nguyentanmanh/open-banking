package com.manh.openbanking.adapter.out.messaging;

import com.manh.openbanking.application.command.BalanceChangeSubmission;
import com.manh.openbanking.domain.event.BalanceChangedEvent;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KafkaBalanceEventPublisherTest {
    @Test
    void publishesContractEnvelopeWithHashedAccountPartitionKey() {
        @SuppressWarnings("unchecked")
        KafkaTemplate<String, BalanceChangedMessage> template = mock(KafkaTemplate.class);
        var metadata = new RecordMetadata(new TopicPartition("balance-topic", 1), 7, 0, 0, 0, 0);
        var result = new SendResult<String, BalanceChangedMessage>(
            new ProducerRecord<>("balance-topic", "key", null), metadata);
        when(template.send(eq("balance-topic"), any(String.class), any(BalanceChangedMessage.class)))
            .thenReturn(CompletableFuture.completedFuture(result));

        var publisher = new KafkaBalanceEventPublisher(template,
            new KafkaBalanceEventProperties("balance-topic", Duration.ofSeconds(1)));
        var event = new BalanceChangedEvent("evt-001", "ACC-0012345678", "txn-001",
            new BigDecimal("150000"), "VND", BalanceChangedEvent.Direction.CREDIT,
            new BigDecimal("1250000"), OffsetDateTime.parse("2026-10-04T15:00:00Z"));

        publisher.publish(new BalanceChangeSubmission(event, "Bearer token", "req-001",
            "2026-10-04T15:00:00Z", "BANK0001", "123456789012345", "signature"));

        var keyCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        var messageCaptor = org.mockito.ArgumentCaptor.forClass(BalanceChangedMessage.class);
        verify(template).send(eq("balance-topic"), keyCaptor.capture(), messageCaptor.capture());
        assertThat(keyCaptor.getValue()).hasSize(64).doesNotContain("ACC-0012345678");
        assertThat(messageCaptor.getValue().eventType()).isEqualTo("balance.changed");
        assertThat(messageCaptor.getValue().correlationId()).isEqualTo("req-001");
        assertThat(messageCaptor.getValue().data().accountId()).isEqualTo("ACC-0012345678");
    }
}
