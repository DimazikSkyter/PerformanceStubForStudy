package ru.performance.transactionhandler;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.KafkaTemplate;
import ru.performance.transactionhandler.consumer.KafkaTransactionalConsumer;
import ru.performance.transactionhandler.producer.KafkaProducer;
import ru.performance.transactionhandler.service.TransactionalEventService;

@Configuration
@EnableKafka
public class KafkaConfig {

    @Bean
    public KafkaTransactionalConsumer kafkaTransactionalConsumer(TransactionalEventService transactionalEventService) {
        return new KafkaTransactionalConsumer(transactionalEventService);
    }

    @Bean
    public KafkaProducer kafkaProducer(KafkaTemplate<Long, byte[]> kafkaTemplate) {
        return new KafkaProducer(kafkaTemplate);
    }
}
