package ru.nspk.performance.transactionshandler.validator;

public interface Validator <M> {

    void validate(M model);
}
