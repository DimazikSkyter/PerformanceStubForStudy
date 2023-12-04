package ru.study.api.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import ru.nspk.performance.api.TicketRequest;

import java.util.concurrent.CompletableFuture;

public class KafkaProducerImpl implements QueueProducer {

    private final String topicName;
    private final KafkaTemplate<Long, byte[]> template;

    public KafkaProducerImpl(String topicName, KafkaTemplate<Long, byte[]> template) {
        this.topicName = topicName;
        this.template = template;
    }

    @Override
    public CompletableFuture<SendResult<Long, byte[]>> send(TicketRequest ticketRequest) {
        return template.send(topicName, ticketRequest.getRequestId(), ticketRequest.toByteArray());
    }
}
