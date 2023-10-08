package ru.nspk.performance.transactionshandler.producer;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import ru.nspk.performance.model.NewTransactionEvent;
import ru.nspk.performance.transactionshandler.KafkaConfig;
import ru.nspk.performance.transactionshandler.properties.KafkaProperties;
import ru.nspk.performance.transactionshandler.schemaregistry.SchemaRegistryContainer;
import ru.nspk.performance.transactionshandler.service.TransactionalEventService;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@ActiveProfiles(profiles = {"kafka"})
@Testcontainers
@SpringBootTest(classes = {KafkaConfig.class, KafkaProperties.class, KafkaAutoConfiguration.class, KafkaProducerTest.TestConfig.class})
@EnableConfigurationProperties
class KafkaProducerTest {

    public static final String CONFLUENT_PLATFORM_VERSION = "6.2.1";


    private static final SchemaRegistryContainer SCHEMA_REGISTRY = new SchemaRegistryContainer(CONFLUENT_PLATFORM_VERSION);

    @Container
    static KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:" + CONFLUENT_PLATFORM_VERSION)).withEnv("KAFKA_CREATE_TOPICS", "test_topic").withExposedPorts(2181, 9092, 9093);
    @MockBean
    TransactionalEventService transactionalEventService;
    @Autowired
    KafkaTemplate<String, NewTransactionEvent> kafkaTemplate;
    @Autowired
    KafkaConsumer<String, ?> newTransactionEventKafkaConsumer;

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        SCHEMA_REGISTRY.withKafka(kafkaContainer).start();
        registry.add("spring.kafka.properties.schema.registry.url", () -> "http//%s:%s".formatted(SCHEMA_REGISTRY.getHost(), SCHEMA_REGISTRY.getFirstMappedPort()));
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
    }

    @Test
    void sendEvent() throws ExecutionException, InterruptedException {

        newTransactionEventKafkaConsumer.assign(Collections.singleton(new TopicPartition("test_topic", 0)));
        CompletableFuture<SendResult<String, NewTransactionEvent>> test_topic = kafkaTemplate.send("test_topic", NewTransactionEvent.newBuilder().setEventId(33).build());
        test_topic.get();
//        while (true) {
//            ConsumerRecords<String, NewTransactionEvent> records = newTransactionEventKafkaConsumer.poll(Duration.ofMillis(100));
//            for (ConsumerRecord<String, NewTransactionEvent> record : records) {
//                System.out.println("Event id: " + record.value().getEventId());
//            }
//            if (!records.isEmpty()) {
//                break;
//            }
//            newTransactionEventKafkaConsumer.commitAsync();
//        }
    }

    @TestConfiguration
    public static class TestConfig {

        @Bean
        public KafkaConsumer<String, NewTransactionEvent> newTransactionEventKafkaConsumer(ConsumerFactory<?, ?> consumerFactory) {
            return (KafkaConsumer<String, NewTransactionEvent>) consumerFactory.createConsumer();
        }

//        @Bean
//        KafkaConsumer<String, NewTransactionEvent> newTransactionEventKafkaConsumer(KafkaProperties kafkaProperties) {
//            Map<String, Object> props = new HashMap<>();
//            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapAddress());
//            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
//            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaProtobufDeserializer.class);
//            props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-consumer-group");
//            props.put(
//                    ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
//                    "earliest");
//            props.put("schema.registry.url", "mock://not-used");
//
//            StringDeserializer stringDeserializer = new StringDeserializer();
//
//            KafkaProtobufDeserializer<NewTransactionEvent> newTransactionEventKafkaProtobufDeserializer =
//                    new KafkaProtobufDeserializer<>(new MockSchemaRegistryClient(Collections.singletonList(new ProtobufSchemaProvider())));
//
//            return new KafkaConsumer<String, NewTransactionEvent>(props, stringDeserializer, newTransactionEventKafkaProtobufDeserializer);
//        }
    }
}