package ru.performance.transactionhandler.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import ru.performance.transactionhandler.properties.KafkaProperties;
import ru.performance.transactionhandler.state.TicketTransactionState;

@RequiredArgsConstructor
public class KafkaProducer {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final KafkaTemplate<Long, byte[]> template;
    private KafkaProperties kafkaProperties;

    public ListenableFuture<SendResult<Long, byte[]>> sendEvent(Long key, byte[] value) {
        return template.send(kafkaProperties.getEventTopic(), key, value);
    }

    public ListenableFuture<SendResult<Long, byte[]>> sendTransactionState(TicketTransactionState ticketTransactionState) {
        return template.send(kafkaProperties.getTransactionStateTopic(), ticketTransactionState.getBytes());
    }

    public ListenableFuture<SendResult<Long, byte[]>> sendPaymentLink(byte[] paymentLinkBytes) {
        return template.send(kafkaProperties.getEventTopic(), paymentLinkBytes);
    }
}
