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
import ru.nspk.performance.action.CreateReserveAction;
import ru.nspk.performance.transactionshandler.model.Seat;
import ru.nspk.performance.transactionshandler.model.theatrecontract.Event;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
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

        Date eventDate = Date.from(Instant.parse("2023-12-03T00:00:00.00Z"));
        Instant start = Instant.now();
        List<Seat> seats = List.of(new Seat("1A", 13.1, "user1"), new Seat("2A", 13.2, "user2"));

        TicketTransactionState ticketTransactionState = TicketTransactionState.builder()
                .transactionId(123131541)
                .currentState(TransactionState.COMPLETE)
                .event(new Event(eventDate, "event name", seats))
                .actions(Map.of("First", CreateReserveAction.builder().seats(seats.stream().map(Seat::place).reduce((s, s2) -> s + "," + s2).get()).build()))
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
        if (stage != 6) {
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