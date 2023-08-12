package ru.nspk.performance.transactionshandler.validator;

public class ValidationException extends RuntimeException{

    public ValidationException(String message) {
        super(message);
    }
}
