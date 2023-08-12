package ru.nspk.performance.transactionshandler.producer;

import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import ru.nspk.performance.model.NewTransactionEvent;
import ru.nspk.performance.transactionshandler.KafkaConfig;
import ru.nspk.performance.transactionshandler.properties.KafkaProperties;
import ru.nspk.performance.transactionshandler.service.TransactionalEventService;

import java.time.Duration;
import java.util.Collections;

@ActiveProfiles(profiles = {"kafka"})
@SpringBootTest(classes = {KafkaConfig.class, KafkaProperties.class, KafkaAutoConfiguration.class, KafkaIOTest.TestConfig.class})
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://${spring.kafka.consumer.bootstrap-servers}"})
public class KafkaIOTest {

    @MockBean
    TransactionalEventService transactionalEventService;

    @Autowired
    KafkaTemplate<String, byte[]> kafkaTemplate;

    @Autowired
    KafkaConsumer<String, byte[]> newTransactionEventKafkaConsumer;

    @Test
    void test() throws InvalidProtocolBufferException {
        newTransactionEventKafkaConsumer.assign(Collections.singleton(new TopicPartition("test_topic", 0)));
        kafkaTemplate.send("test_topic",  NewTransactionEvent.newBuilder().setEventId(33L).build().toByteArray());
        while (true) {
            ConsumerRecords<String, byte[]> records = newTransactionEventKafkaConsumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<String, byte[]> record : records) {
                System.out.println("Event id: " + NewTransactionEvent.parseFrom(record.value()).getEventId());
            }
            if (!records.isEmpty()) {
                break;
            }
            newTransactionEventKafkaConsumer.commitAsync();
        }
    }

    @TestConfiguration
    public static class TestConfig {

        @Bean
        public KafkaConsumer<String, byte[]> newTransactionEventKafkaConsumer(ConsumerFactory<?, ?> consumerFactory) {
            return (KafkaConsumer<String, byte[]>) consumerFactory.createConsumer();
        }

    }
}
