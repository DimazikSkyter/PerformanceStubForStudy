package ru.study;


import com.google.protobuf.InvalidProtocolBufferException;
import junit.framework.AssertionFailedError;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import ru.nspk.performance.api.TicketRequest;
import ru.study.api.TicketStoreApp;
import ru.study.api.dto.EventDto;
import ru.study.api.dto.SeatDto;
import ru.study.api.dto.TicketDto;
import ru.study.api.dto.TicketPurchaseRequest;
import ru.nspk.performance.keyvaluestorage.model.Person;

import java.time.Duration;
import java.util.*;

@Slf4j
@Testcontainers
@AutoConfigureWebTestClient
@SpringBootTest(classes = {TicketStoreApp.class, IntegrationTest.TestConfig.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IntegrationTest {

    @Container
    static final KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:5.1.2")
    );

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    KafkaConsumer<Long, byte[]> ticketRequestKafkaConsumer;

    @Autowired
    KafkaTemplate<Long, byte[]> kafkaTemplate;

    @Test
    void shouldBuyTicket() throws InvalidProtocolBufferException {
        double price = 11.3;
        String place = "4B";

        Person person = Person.builder()
                .fio("Fio of him")
                .passportCode("1123 303030")
                .build();
        SeatDto seatDto = new SeatDto(place, price);
        TicketPurchaseRequest ticketPurchaseRequest = new TicketPurchaseRequest("event name", List.of(new TicketDto(person, seatDto)));
        webTestClient.put()
                .uri("/storage/api/ticket/purchase")
                .body(BodyInserters.fromValue(ticketPurchaseRequest))
                .exchange()
                .expectBody()
                .consumeWith(System.out::println);


        ticketRequestKafkaConsumer.assign(Collections.singleton(new TopicPartition("ticket_request", 0)));
        try {
            for (int i = 0; i < 10; i++) {
                ConsumerRecords<Long, byte[]> records = ticketRequestKafkaConsumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<Long, byte[]> record : records) {
                    TicketRequest ticketRequest = TicketRequest.parseFrom(record.value());
                    Assertions.assertEquals(0, ticketRequest.getRequestId());
                    Assertions.assertEquals((float) price, ticketRequest.getTicketInfo(0).getPrice());
                    Assertions.assertEquals(place, ticketRequest.getTicketInfo(0).getPlace());
                    Assertions.assertEquals("2023-07-01", ticketRequest.getEventDate());
                    log.info("Income ticket request\n{}", ticketRequest);
                    ticketRequestKafkaConsumer.close();
                    return;
                }
                ticketRequestKafkaConsumer.commitAsync();
            }
            kafka.stop();
        } catch (AssertionFailedError | Exception e) {
            ticketRequestKafkaConsumer.close();
            kafka.stop();
            throw e;
        }
        Assertions.fail("No records income from kafka");

    }

    @TestConfiguration
    public static class TestConfig {

        @Bean
        public KafkaConsumer<Long, byte[]> ticketRequestKafkaConsumer(ConsumerFactory<?, ?> consumerFactory) {
            return (KafkaConsumer<Long, byte[]>) consumerFactory.createConsumer();
        }

    }
}
