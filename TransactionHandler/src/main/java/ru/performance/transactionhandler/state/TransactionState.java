package ru.performance.transactionhandler.state;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum TransactionState {
    NEW_TRANSACTION(0),
    RESERVED(1),
    WAIT_FOR_PAYMENT(2),
    WAIT_MERCHANT_APPROVE(3),
    COMPLETE(4),
    REJECT(4);

    private final int stage;

    public boolean isEarliestThan(TransactionState transactionState) {
        return transactionState != null && stage < transactionState.stage;
    }
}
