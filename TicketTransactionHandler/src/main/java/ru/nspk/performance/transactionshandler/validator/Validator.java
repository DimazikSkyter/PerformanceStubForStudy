package ru.nspk.performance.transactionshandler.validator;

import lombok.NonNull;

public interface Validator<M> {

    void validate(@NonNull M model);
}
