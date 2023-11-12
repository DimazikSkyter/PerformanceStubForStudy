package ru.nspk.performance.transactionshandler.state;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.nspk.performance.events.CreateReserveAction;
import ru.nspk.performance.transactionshandler.model.theatrecontract.Event;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
class TicketTransactionStateTest {

    ObjectMapper objectMapper = new ObjectMapper()
            .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
            .registerModule(new JavaTimeModule());

    private static Stream<Arguments> ticketTransactionState() {
        return Arrays.stream(TransactionState.values()).map(transactionState -> Arguments.of(TicketTransactionState
                .builder()
                .currentState(transactionState)
                .build()));
    }

    @Test
    void shouldTicketTransactionStateSerializeDeserialize() throws IOException {

        Instant eventDate = Instant.now();
        Instant start = Instant.now();
        List<String> seats = List.of("1A", "2B");

        TicketTransactionState ticketTransactionState = TicketTransactionState.builder()
                .transactionId(123131541)
                .currentState(TransactionState.COMPLETE)
                .event(new Event(eventDate, "event name", seats))
                .actions(Map.of("First", CreateReserveAction.builder().seats(seats).eventId(1).build()))
                .start(start)
                .build();

        TicketTransactionState ttsFromBytes = objectMapper
                .readValue(ticketTransactionState.getBytes(), TicketTransactionState.class);
        System.out.println(ttsFromBytes);
        Assertions.assertEquals(ticketTransactionState, ttsFromBytes);
    }

    @ParameterizedTest
    @MethodSource("ticketTransactionState")
    void shouldMoveOnNextState(TicketTransactionState ticketTransactionState) {
        log.info("Test state move for state {}", ticketTransactionState.getCurrentState());
        int stage = ticketTransactionState.getCurrentState().getStage();
        if (stage != 5) {
            ticketTransactionState.moveOnNextStep(ticketTransactionState.getCurrentState());
            Assertions.assertEquals(++stage, ticketTransactionState.getCurrentState().getStage());
            log.info("New state is {}", ticketTransactionState.getCurrentState());
        } else {
            TransactionState transactionState = ticketTransactionState.getCurrentState();
            Assertions.assertThrows(WrongTransactionStateException.class,
                    () -> ticketTransactionState.moveOnNextStep(transactionState));
        }
    }

    @Test
    void shouldntMoveTicketTransactionStateWithWrongState() {
        TicketTransactionState ticketTransactionState = TicketTransactionState.builder()
                .currentState(TransactionState.RESERVED)
                .build();
        Assertions.assertThrows(
                WrongTransactionStateException.class,
                () -> ticketTransactionState.moveOnNextStep(TransactionState.COMPLETE)
        );
    }
}