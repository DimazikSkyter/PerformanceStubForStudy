package ru.nspk.performance.transactionshandler.validator;

import org.apache.logging.log4j.util.Strings;

import java.util.List;

public class ValidationException extends RuntimeException{

    private ValidationError validationError;

    public ValidationException(List<String> failedPatterns, String validatorName) {
        super(String.format("Failed patterns '%s' in validator %s", Strings.join(failedPatterns, ','), validatorName));
    }

    public ValidationException(String message, String validatorName, ValidationError validationError) {
        super(String.format("Catch exception %s in validator %s", message, validatorName));
        this.validationError = validationError;
    }
}
