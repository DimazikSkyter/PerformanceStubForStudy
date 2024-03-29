package ru.nspk.performance.transactionshandler.state;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum TransactionState {
    NEW_TRANSACTION(0),

    RESERVE_REQUEST(1),
    RESERVED(2),
    WAIT_FOR_PAYMENT_LINK(3),
    PAYMENT_LINK_CREATED(4),
    WAIT_FOR_PAYMENT(5),
    NOTIFY_THEATRE(6),
    NOTIFY_THEATRE_SEND(7),
    COMPLETE(8),
    REJECT(8);

    @Getter
    private final int stage;

    public boolean isEarliestThan(TransactionState transactionState) {
        return transactionState != null && stage < transactionState.stage;
    }
}
