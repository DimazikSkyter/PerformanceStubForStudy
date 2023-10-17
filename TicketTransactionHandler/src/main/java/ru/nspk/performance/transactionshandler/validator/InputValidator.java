package ru.nspk.performance.transactionshandler.validator;

import lombok.NonNull;

public interface InputValidator {

    void validateInput(@NonNull String input);
}
