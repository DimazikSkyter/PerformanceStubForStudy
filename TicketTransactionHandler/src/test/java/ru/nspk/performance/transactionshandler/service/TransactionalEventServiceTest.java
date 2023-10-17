package ru.nspk.performance.transactionshandler.service;

import com.google.protobuf.Timestamp;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.nspk.performance.api.TicketRequest;
import ru.nspk.performance.base.PlaceCoordinate;
import ru.nspk.performance.transactionshandler.keyvaluestorage.KeyValueStorage;
import ru.nspk.performance.transactionshandler.model.theatrecontract.Event;
import ru.nspk.performance.transactionshandler.producer.KafkaProducer;
import ru.nspk.performance.transactionshandler.properties.TransactionProperties;
import ru.nspk.performance.transactionshandler.state.TicketTransactionState;
import ru.nspk.performance.transactionshandler.state.TransactionState;
import ru.nspk.performance.transactionshandler.theatreclient.TheatreClient;
import ru.nspk.performance.transactionshandler.timeoutprocessor.TimeoutProcessor;
import ru.nspk.performance.transactionshandler.transformer.TransformerMultiton;
import ru.nspk.performance.transactionshandler.validator.ValidatorMultiton;

import java.time.Instant;
import java.util.List;

@Slf4j
@ExtendWith(MockitoExtension.class)
class TransactionalEventServiceTest {

    @Mock
    private KafkaProducer kafkaProducer;

    @Mock
    private ValidatorMultiton validators;
    @Mock
    private TheatreClient theatreClient;
    @Mock
    private TransformerMultiton transformers;
    @Mock
    private KeyValueStorage keyValueStorage;
    @Mock
    private TimeoutProcessor timeoutProcessor;
    @Mock
    private TransactionProperties transactionProperties;
    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private TransactionalEventService transactionalEventService;

    @Test
    void shouldPositiveHandleNewTicketEvent() {
        log.info("Start new ticket event test");
        TicketRequest ticketRequest = TicketRequest.newBuilder()
                .setRequestId(311)
                .setEventDate("2023-11-01")
                .setEventName("Rock-roll")
                .addPlaceCoordinate(PlaceCoordinate.newBuilder()
                        .setRow(3)
                        .setPlace(11)
                        .build())
                .addPlaceCoordinate(PlaceCoordinate.newBuilder()
                        .setRow(3)
                        .setPlace(12)
                        .build())
                .setTimestamp(Timestamp.newBuilder()
                        .setSeconds(Instant.now().getEpochSecond())
                        .build())
                .build();
        TicketTransactionState ticketTransactionState = TicketTransactionState
                .builder()
                .transactionId(55555L)
                .event(new Event(Instant.now(), "Rock-roll", List.of(
                        "3:11", "3:12"
                )))
                .currentState(TransactionState.NEW_TRANSACTION)
                .build();

        Mockito.doReturn(ticketTransactionState).when(transformers).transform(ticketRequest);

        transactionalEventService.newTicketEvent(ticketRequest);
    }

    @Test
    void handleReserveResponse() {
    }

    @Test
    void handlePaymentResponse() {
    }

    @Test
    void handleReversePayment() {
    }

    @Test
    void rejectTransaction() {
    }

    @Test
    void makeReserve() {
    }
}