package ru.nspk.performance.transactionshandler.state;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import lombok.Builder;
import lombok.Data;
import ru.nspk.performance.events.Action;
import ru.nspk.performance.transactionshandler.model.theatrecontract.Event;

import java.io.IOException;
import java.io.Serializable;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;


//todo перенести в common
@Builder
@Data
public class TicketTransactionState implements TicketFlow, Serializable {

    @JsonIgnore
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
            .registerModule(new JavaTimeModule());

    @JsonIgnore
    private final Map<TransactionState, TransactionState> ticketFlow = Map.of(
            TransactionState.NEW_TRANSACTION, TransactionState.RESERVE_REQUEST,
            TransactionState.RESERVE_REQUEST, TransactionState.RESERVED,
            TransactionState.RESERVED, TransactionState.WAIT_FOR_PAYMENT,
            TransactionState.WAIT_FOR_PAYMENT, TransactionState.WAIT_MERCHANT_APPROVE,
            TransactionState.WAIT_MERCHANT_APPROVE, TransactionState.COMPLETE
    );


    private long transactionId;
    private Map<String, Action> actions;
    private TransactionState currentState;
    private String errorReason;
    private Instant start;
    private Event event;

    @JsonCreator
    public TicketTransactionState(
            @JsonProperty("transactionId") long transactionId,
            @JsonProperty("actions") Map<String, Action> actions,
            @JsonProperty("currentState") TransactionState currentState,
            @JsonProperty(value = "errorReason") String errorReason,
            @JsonProperty("start") Instant start,
            @JsonProperty("event") Event event
    ) {
        this.transactionId = transactionId;
        this.actions = actions;
        this.currentState = currentState;
        this.errorReason = errorReason;
        this.start = start;
        this.event = event;
    }

    public static TicketTransactionState from(byte[] bytes) throws IOException {
        return OBJECT_MAPPER.readValue(bytes, TicketTransactionState.class);
    }

    public byte[] getBytes() throws JsonProcessingException {
        return OBJECT_MAPPER.writeValueAsBytes(this);
    }

    @Override
    public void rejectTransaction(String reason) {
        currentState = TransactionState.REJECT;
        this.errorReason = reason;
    }

    @Override
    public void moveOnNextStep(TransactionState receivedCurrentState) {
        if (this.currentState.equals(receivedCurrentState)) {
            this.currentState = Optional.ofNullable(ticketFlow.get(receivedCurrentState)).orElseThrow(
                    () -> new WrongTransactionStateException(receivedCurrentState, currentState)
            );
        } else {
            throw new WrongTransactionStateException(receivedCurrentState, currentState);
        }
    }
}
