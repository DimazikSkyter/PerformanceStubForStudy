package ru.study.stub.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.util.concurrent.ListenableFuture;
import ru.study.stub.proto.Ticket;

public class KafkaProducerImpl implements QueueProducer {

    private final String topicName;
    private final KafkaTemplate<String, Ticket> template;

    public KafkaProducerImpl(String topicName, KafkaTemplate<String, Ticket> template) {
        this.topicName = topicName;
        this.template = template;
    }

    @Override
    public ListenableFuture send(Ticket ticket) {
        return template.send(topicName, ticket);
    }
}
