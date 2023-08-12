package ru.nspk.performance.transactionshandler;


import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.protobuf.ProtobufSchemaProvider;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializerConfig;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import ru.nspk.performance.model.NewTransactionEvent;
import ru.nspk.performance.transactionshandler.consumer.KafkaTransactionalConsumer;
import ru.nspk.performance.transactionshandler.producer.KafkaNewTransactionEventProducer;
import ru.nspk.performance.transactionshandler.properties.KafkaProperties;
import ru.nspk.performance.transactionshandler.service.TransactionalEventService;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializerConfig.SPECIFIC_PROTOBUF_VALUE_TYPE;
import static org.apache.kafka.streams.StreamsConfig.*;

@Configuration
@EnableKafka
public class KafkaConfig {

    @Bean
    public KafkaTransactionalConsumer kafkaTransactionalConsumer(TransactionalEventService transactionalEventService) {
        return new KafkaTransactionalConsumer(transactionalEventService);
    }

//    @Bean
//    public ProducerFactory<String, NewTransactionEvent> kafkaProducerFactory(KafkaProperties kafkaProperties) {
//        Map<String, Object> props = new HashMap<>();
//        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapAddress());
//        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
//        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaProtobufSerializer.class);
//
//        Supplier<Serializer<String>> keySerializer = StringSerializer::new;
//        Supplier<Serializer<NewTransactionEvent>> valueSerializer = () ->
//                new KafkaProtobufSerializer<NewTransactionEvent>(new MockSchemaRegistryClient(Collections.singletonList(new ProtobufSchemaProvider())));
//
//        props.put("schema.registry.url", "mock://not-used");
//        return new DefaultKafkaProducerFactory<>(props, keySerializer, valueSerializer);
//    }

//    @Bean
//    public KafkaTemplate<String, NewTransactionEvent> kafkaTemplate(ProducerFactory<String, NewTransactionEvent> kafkaProducerFactory) {
//        return new KafkaTemplate<>(kafkaProducerFactory);
//    }

//    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
//    public KafkaStreamsConfiguration kafkaStreamsConfiguration(KafkaProperties kafkaProperties) {
//
//        Map<String, Object> props = new HashMap<>();
//        props.put(APPLICATION_ID_CONFIG, "streams-app");
//        props.put(BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapAddress());
//        props.put(DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
//        props.put(DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
//        return new KafkaStreamsConfiguration(props);
//    }

    @Bean
    public KafkaNewTransactionEventProducer kafkaNewTransactionEventProducer(KafkaTemplate<String, ?> newTransactionEventKafkaTemplate) {
        return new KafkaNewTransactionEventProducer((KafkaTemplate<String, NewTransactionEvent>) newTransactionEventKafkaTemplate);
    }
}
