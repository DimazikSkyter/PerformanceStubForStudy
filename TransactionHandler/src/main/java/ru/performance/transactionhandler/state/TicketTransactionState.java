package ru.performance.transactionhandler.state;

import lombok.Data;
import ru.performance.transactionhandler.model.Event;

import java.io.Serializable;
import java.util.Map;

@Data
public class TicketTransactionState implements TicketFlow, Serializable {
    
    private Map<TransactionState, TransactionState> ticketFlow = Map.of(
            TransactionState.NEW_TRANSACTION, TransactionState.RESERVED,
            TransactionState.RESERVED, TransactionState.WAIT_FOR_PAYMENT,
            TransactionState.WAIT_FOR_PAYMENT, TransactionState.COMPLETE
    );


    private long transactionId;
    private Map<String, Event> events;
    private TransactionState currentState = TransactionState.NEW_TRANSACTION;
    private String errorReason;

    public void lock() {
        //todo Реализовать однопоточную обработку тикеттранзакшена
    }

    public void release() {
        //todo
    }

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
