package ru.nspk.performance.transactionshandler;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.KafkaTemplate;
import ru.nspk.performance.model.NewTransactionEvent;
import ru.nspk.performance.transactionshandler.consumer.KafkaTransactionalConsumer;
import ru.nspk.performance.transactionshandler.producer.KafkaProducer;
import ru.nspk.performance.transactionshandler.properties.KafkaProperties;
import ru.nspk.performance.transactionshandler.service.TransactionalEventService;

@Configuration
@EnableKafka
public class KafkaConfig {

    @Bean
    public KafkaTransactionalConsumer kafkaTransactionalConsumer(TransactionalEventService transactionalEventService) {
        return new KafkaTransactionalConsumer(transactionalEventService);
    }

    @Bean
    public KafkaProducer kafkaProducer(KafkaTemplate<Long, byte[]> kafkaTemplate,
                                       KafkaProperties kafkaProperties) {
        return new KafkaProducer(kafkaTemplate, kafkaProperties);
    }
}
