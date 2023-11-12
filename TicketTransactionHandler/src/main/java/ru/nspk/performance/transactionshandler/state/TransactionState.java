package ru.nspk.performance.transactionshandler.state;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum TransactionState {
    NEW_TRANSACTION(0),

    RESERVE_REQUEST(1),
    RESERVED(2),
    WAIT_FOR_PAYMENT(3),
    WAIT_MERCHANT_APPROVE(4),
    COMPLETE(5),
    REJECT(5);

    @Getter
    private final int stage;

    public boolean isEarliestThan(TransactionState transactionState) {
        return transactionState != null && stage < transactionState.stage;
    }
}
