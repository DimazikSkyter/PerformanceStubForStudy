package ru.nspk.performance.transactionshandler.producer;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import ru.nspk.performance.transactionshandler.properties.KafkaProperties;

@RequiredArgsConstructor
public class KafkaProducer {

    private final KafkaTemplate<String, byte[]> template;
    private KafkaProperties kafkaProperties;

    public ListenableFuture<SendResult<String, byte[]>> sendEvent(String key, byte[] value) {
        return template.send(kafkaProperties.getEventTopic(), key, value);
    }
}
