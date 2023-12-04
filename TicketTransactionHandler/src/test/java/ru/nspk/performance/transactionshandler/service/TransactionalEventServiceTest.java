package ru.nspk.performance.transactionshandler.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.nspk.performance.api.TicketRequest;
import ru.nspk.performance.base.TicketInfo;
import ru.nspk.performance.keyvaluestorage.KeyValueStorage;
import ru.nspk.performance.transactionshandler.model.Seat;
import ru.nspk.performance.transactionshandler.model.theatrecontract.Event;
import ru.nspk.performance.transactionshandler.payment.PaymentClient;
import ru.nspk.performance.transactionshandler.producer.KafkaProducer;
import ru.nspk.performance.transactionshandler.properties.InMemoryProperties;
import ru.nspk.performance.transactionshandler.properties.TransactionProperties;
import ru.nspk.performance.transactionshandler.state.TicketTransactionState;
import ru.nspk.performance.transactionshandler.state.TransactionState;
import ru.nspk.performance.transactionshandler.theatreclient.TheatreClient;
import ru.nspk.performance.transactionshandler.timeoutprocessor.TimeoutProcessor;
import ru.nspk.performance.transactionshandler.transformer.TransformerMultiton;
import ru.nspk.performance.transactionshandler.validator.ValidatorMultiton;

import java.sql.Date;
import java.text.ParseException;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


//todo Сейчас ЗДЕСЬ
@Slf4j
@SpringBootTest(classes = {TransactionalEventService.class, TransactionalEventServiceTest.TransactionEventServiceTestConfig.class, InMemoryProperties.class})
@ExtendWith(SpringExtension.class)
@Profile(value = "tes")
@EnableConfigurationProperties
class TransactionalEventServiceTest {

    @MockBean
    private KafkaProducer kafkaProducer;

    @MockBean
    private ValidatorMultiton validators;
    @MockBean
    private TheatreClient theatreClient;
    @MockBean
    private TransformerMultiton transformers;
    @MockBean
    private KeyValueStorage keyValueStorage;
    @MockBean
    private TimeoutProcessor timeoutProcessor;
    @MockBean
    private TransactionProperties transactionProperties;
    @MockBean
    private PaymentClient paymentClient;

    @Autowired
    private TransactionalEventService transactionalEventService;

    @Test
    void shouldPositiveHandleNewTicketEvent() throws InterruptedException, ParseException, JsonProcessingException {
        log.info("Start new ticket event test");
        TicketRequest ticketRequest = TicketRequest.newBuilder()
                .setRequestId(311)
                .setEventDate("2023-11-01")
                .setEventName("Rock-roll")
                .addTicketInfo(TicketInfo.newBuilder()
                        .setPrice(33.2F)
                        .setPlace("4A")
                        .setUserUUID("user-1")
                        .build())
                .build();
        TicketTransactionState ticketTransactionState = TicketTransactionState
                .builder()
                .transactionId(55555L)
                .event(new Event(Date.from(Instant.now()), "Rock-roll", List.of(
                        new Seat("3:11", 3.11, "asdsad"), new Seat("3:12", 3.12, "bbxc")
                )))
                .currentState(TransactionState.NEW_TRANSACTION)
                .build();

        Mockito.doReturn(ticketTransactionState).when(transformers).transform(ticketRequest);

        transactionalEventService.newTicketEvent(ticketRequest);
    }

    @TestConfiguration
    @EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
    public static class TransactionEventServiceTestConfig {

        @Bean
        public TimeoutProcessor timeoutProcessor(TransactionProperties transactionProperties) {
            ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(transactionProperties.getTimeoutProcessorThreads());
            return new TimeoutProcessor(scheduledExecutorService);
        }
    }
}