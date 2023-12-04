package ru.nspk.performance.transactionshandler.state;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import ru.nspk.performance.action.Action;
import ru.nspk.performance.transactionshandler.model.theatrecontract.Event;

import java.io.IOException;
import java.io.Serializable;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;


//todo перенести в common
@Builder
@Data
@ToString(exclude = {"ticketFlow"})
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
            TransactionState.RESERVED, TransactionState.WAIT_FOR_PAYMENT_LINK,
            TransactionState.WAIT_FOR_PAYMENT_LINK, TransactionState.PAYMENT_LINK_CREATED,
            TransactionState.PAYMENT_LINK_CREATED, TransactionState.WAIT_FOR_PAYMENT,
            TransactionState.WAIT_FOR_PAYMENT, TransactionState.COMPLETE
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


    //todo подумать об ordinal в enum
    //todo может быть гонка состояний,  нужно сделать либо меньше, либо текущий. Далее нужно сохранить экшен в KV, но дальше идти не нужно
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
