package ru.nspk.performance.transactionshandler.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import ru.nspk.performance.transactionshandler.properties.KafkaProperties;

import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
public class KafkaProducer {

    private final KafkaTemplate<Long, byte[]> template;
    private final KafkaProperties kafkaProperties;

    public CompletableFuture<SendResult<Long, byte[]>> sendAction(Long key, byte[] value) {
        return template.send(kafkaProperties.getActionTopic(), key, value);
    }

    public CompletableFuture<SendResult<Long, byte[]>> sendTransactionState(Long transactionId, byte[] bytes) throws JsonProcessingException {
        log.debug("Send transaction state with id {} to topic {}", transactionId, kafkaProperties.getTransactionStateTopic());
        return template.send(kafkaProperties.getTransactionStateTopic(), bytes);
    }

    public CompletableFuture<SendResult<Long, byte[]>> sendCompleteAction(byte[] bytes) {
        return template.send(kafkaProperties.getActionTopic(), bytes);
    }

    public CompletableFuture<SendResult<Long, byte[]>> sendPaymentLinkForApi(byte[] bytes) {
        return template.send(kafkaProperties.getPaymentLinkTopic(), bytes);
    }
}
