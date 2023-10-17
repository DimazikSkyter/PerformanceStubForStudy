package ru.nspk.performance.transactionshandler.validator;

import lombok.NonNull;
import org.apache.commons.lang3.tuple.Pair;
import ru.nspk.performance.transactionshandler.model.PaymentCheckResponse;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class PaymentCheckResponseInputValidator implements InputValidator {

    private List<Pair<String, Pattern>> patterns;

    public PaymentCheckResponseInputValidator(@NonNull List<Pair<String, String>> patternsStr) {
        patterns = patternsStr.stream()
                .map(patternStr -> Pair.of(patternStr.getLeft(), Pattern.compile(patternStr.getRight())))
                .collect(Collectors.toList());
    }

    @Override
    public void validateInput(@NonNull String input) {
        List<String> failedPatterns = patterns.stream()
                .map(patternPair -> Pair.of(patternPair.getLeft(), patternPair.getRight().matcher(input).find()))
                .filter(pair -> !pair.getRight())
                .map(Pair::getLeft)
                .toList();

        if (!failedPatterns.isEmpty()) {
            throw new ValidationException(failedPatterns, this.getClass().getName());
        }
    }
}
