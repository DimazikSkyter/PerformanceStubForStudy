package ru.nspk.performance.transactionshandler.transformer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TransformException extends RuntimeException {

    public TransformException(String data) {
        super("Failed to transform object " + data);
    }
}
