package ru.nspk.performance.transactionshandler.state;

public interface TicketFlow {

    void rejectTransaction(String reason);
    void moveOnNextStep(TransactionState expectedCurrentState);
    TransactionState getCurrentState();
}
