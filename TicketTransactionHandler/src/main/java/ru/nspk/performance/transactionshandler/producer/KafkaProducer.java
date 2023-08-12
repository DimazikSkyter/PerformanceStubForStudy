package ru.nspk.performance.transactionshandler.producer;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import ru.nspk.performance.transactionshandler.properties.KafkaProperties;

@RequiredArgsConstructor
public abstract class KafkaProducer<V> {

    private final KafkaTemplate<String, V> template;
    private KafkaProperties kafkaProperties;

    public ListenableFuture<SendResult<String, V>> sendEvent(String key, V value) {
        return template.send(kafkaProperties.getEventTopic(), key, value);
    }
}
