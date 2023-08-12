package ru.nspk.performance.transactionshandler.state;

public enum TransactionState {
    NEW_TRANSACTION,
    RESERVED,
    WAIT_FOR_PAYMENT, COMPLETE, REJECT;
}
