package ru.performance.transactionhandler.state;

public class WrongTransactionStateException extends RuntimeException {

    public WrongTransactionStateException(TransactionState receivedTransactionState,
                                          TransactionState expectedTransactionState) {
        super(String.format("Wrong transaction state! Expected: %s.\nReceived: %s.",
                expectedTransactionState,
                receivedTransactionState));
    }
}
