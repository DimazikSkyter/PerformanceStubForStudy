package ru.nspk.performance.transactionshandler.validator;


import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class ValidatorMultiton {

    private final Map<Class, InputValidator> inputValidators;
    private final Map<Class, ModelValidator> modelValidators;

    public void validateInput(@NonNull String model, Class cls) {
        inputValidators.get(cls).validateInput(model);
    }

    public void validateModel(@NonNull Object model) throws InterruptedException {
        modelValidators.get(model.getClass()).validateModel(model);
    }
}
