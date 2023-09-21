package ru.nspk.performance.transactionshandler.validator;


import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class ValidatorMultiton {

    private final Map<Class, Validator> validators;

    public void validate(@NonNull Object object) {
        validators.get(object.getClass()).validate(object);
    }
}
