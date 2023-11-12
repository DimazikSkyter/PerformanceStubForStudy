package ru.study.stub;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.study.stub.producer.KafkaProducerImpl;
import ru.study.stub.producer.QueueProducer;

@Configuration
@EnableScheduling
public class TicketStoreConfigurator {


    @Bean
    public QueueProducer kafkaProducer(KafkaTemplate<Long, byte[]> kafkaTemplate) {
        return new KafkaProducerImpl("ticket_request", kafkaTemplate);
    }
}
