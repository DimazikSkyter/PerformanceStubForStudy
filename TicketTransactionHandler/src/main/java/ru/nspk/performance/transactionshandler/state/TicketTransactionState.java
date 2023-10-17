package ru.nspk.performance.transactionshandler.state;

import lombok.Builder;
import lombok.Data;
import ru.nspk.performance.events.Action;
import ru.nspk.performance.transactionshandler.model.theatrecontract.Event;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;

@Builder
@Data
public class TicketTransactionState implements TicketFlow, Serializable {
    
    private final Map<TransactionState, TransactionState> ticketFlow = Map.of(
            TransactionState.NEW_TRANSACTION, TransactionState.RESERVE_REQUEST,
            TransactionState.RESERVE_REQUEST, TransactionState.RESERVED,
            TransactionState.RESERVED, TransactionState.WAIT_FOR_PAYMENT,
            TransactionState.WAIT_FOR_PAYMENT, TransactionState.COMPLETE
    );


    private long transactionId;
    private Map<String, Action> actions;
    private TransactionState currentState;
    private String errorReason;
    private Instant start;
    private Event event;

    public byte[] getBytes() {
        return new byte[1];
    }

    @Override
    public void rejectTransaction(String reason) {
        currentState = TransactionState.REJECT;
        this.errorReason = reason;
    }

    @Override
    public void moveOnNextStep(TransactionState receivedCurrentState) {
        if (this.currentState.equals(receivedCurrentState)) {
            this.currentState = ticketFlow.get(receivedCurrentState);
        } else {
            throw new WrongTransactionStateException(receivedCurrentState, currentState);
        }
    }
}
