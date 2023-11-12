package ru.nspk.performance.transactionshandler.transformer;

import com.google.protobuf.Timestamp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.nspk.performance.api.TicketRequest;
import ru.nspk.performance.base.PlaceCoordinate;
import ru.nspk.performance.transactionshandler.state.TicketTransactionState;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class TransformersTest {

    private Transformer<TicketRequest, TicketTransactionState> ticketRequestTransformer = new TicketRequestTransformer();
    @Test
    public void shouldSuccessTicketRequestTransformer() {
        String eventName = "Rock-roll";
        String eventDate = "2023-11-01";

        TicketRequest ticketRequest = TicketRequest.newBuilder()
                .setRequestId(311)
                .setEventDate(eventDate)
                .setEventName(eventName)
                .addPlaceCoordinate(PlaceCoordinate.newBuilder()
                        .setRow(3)
                        .setUserUUID("user1")
                        .setPrice(31.1F)
                        .setPlace(11)
                        .build())
                .addPlaceCoordinate(PlaceCoordinate.newBuilder()
                        .setRow(3)
                        .setUserUUID("user2")
                        .setPrice(30.1F)
                        .setPlace(12)
                        .build())
                .setStart(Timestamp.newBuilder()
                        .setSeconds(Instant.now().getEpochSecond())
                        .build())
                .build();

        TicketTransactionState ticketTransactionState = ticketRequestTransformer.transform(ticketRequest);

        assertEquals(eventName, ticketTransactionState.getEvent().getEventName());
        assertEquals(Instant.parse(eventDate), ticketTransactionState.getEvent().getEventDate());
//        assertEquals(eventName, ticketTransactionState.getTransactionId());
//        assertEquals(eventName, ticketTransactionState.getEvent().getEventName());
//        assertEquals(eventName, ticketTransactionState.getEvent().getEventName());
    }

}