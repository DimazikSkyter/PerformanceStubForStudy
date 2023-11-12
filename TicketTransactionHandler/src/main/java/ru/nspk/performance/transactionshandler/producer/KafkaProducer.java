package ru.nspk.performance.transactionshandler.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import ru.nspk.performance.transactionshandler.properties.KafkaProperties;
import ru.nspk.performance.transactionshandler.state.TicketTransactionState;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class KafkaProducer {

    private final KafkaTemplate<Long, byte[]> template;
    private KafkaProperties kafkaProperties;

    public CompletableFuture<SendResult<Long, byte[]>> sendEvent(Long key, byte[] value) {
        return template.send(kafkaProperties.getEventTopic(), key, value);
    }

    public CompletableFuture<SendResult<Long, byte[]>> sendTransactionState(TicketTransactionState ticketTransactionState) throws JsonProcessingException {
        return template.send(kafkaProperties.getTransactionStateTopic(), ticketTransactionState.getBytes());
    }

    public CompletableFuture<SendResult<Long, byte[]>> sendPaymentLink(byte[] paymentLinkBytes) {
        return template.send(kafkaProperties.getEventTopic(), paymentLinkBytes);
    }
}
