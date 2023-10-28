package ru.nspk.performance.transactionshandler.validator;

import lombok.NonNull;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class ReserveResponseEventInputValidator extends InputValidator {



    public ReserveResponseEventInputValidator(List<Pair<String, String>> patternsStr) {
        super(patternsStr);
    }
}
