package ru.nspk.performance.transactionshandler.validator;

import lombok.NonNull;

public interface ModelValidator <M>{

    void validateModel(@NonNull M model) throws InterruptedException;
}
