package ru.nspk.performance.transactionshandler.validator;

import lombok.NonNull;
import org.apache.commons.lang3.tuple.Pair;
import ru.nspk.performance.transactionshandler.model.PaymentCheckResponse;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class PaymentCheckResponseInputValidator extends InputValidator {

    public PaymentCheckResponseInputValidator(List<Pair<String, String>> patternsStr) {
        super(patternsStr);
    }
}
