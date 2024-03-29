package ru.nspk.performance.transactionshandler.validator;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Slf4j
class ValidatorsTest {

    private List<Pair<String, String>> paymentCheckResponsePatterns = List.of(
            Pair.of("request_id", "\"request_id\"\\s*:\\s*\\d+"),
            Pair.of("payment_status", "\"payment_status\"\\s*:\\s*(true|false)")
    );

    List<Pair<String, String>> reserveResponseActionPatterns = List.of(
            Pair.of("request_id", "\"request_id\"\\s*:\\s*\\d+"),
            Pair.of("reserve_id", "\"reserve_id\"\\s*:\\s*\\d+")
    );

    @Test
    void paymentCheckResponsePositive() throws IOException {
        String paymentResponse = new String(Objects.requireNonNull(this.getClass()
                        .getClassLoader()
                        .getResourceAsStream("payment-check-response-positive.json"))
                .readAllBytes());
        log.info("Income positive payment response '{}'", paymentResponse);

        InputValidator inputValidator = new InputValidator(paymentCheckResponsePatterns);

        Assertions.assertDoesNotThrow(() -> inputValidator.validateInput(paymentResponse));
    }

    @Test
    void paymentCheckResponseNegative() throws IOException {
        String paymentResponse = new String(Objects.requireNonNull(this.getClass()
                        .getClassLoader()
                        .getResourceAsStream("payment-check-response-negative.json"))
                .readAllBytes());
        log.info("Income negative payment response '{}'", paymentResponse);

        InputValidator inputValidator = new InputValidator(paymentCheckResponsePatterns);
        Assertions.assertThrows(ValidationException.class, () -> inputValidator.validateInput(paymentResponse));
    }

    @Test
    void reserveResponseEventPositive() throws IOException {
        String reserveResponseEvent = new String(Objects.requireNonNull(this.getClass()
                        .getClassLoader()
                        .getResourceAsStream("reserve-response-event-positive.json"))
                .readAllBytes());
        log.info("Income positive reserve response '{}'", reserveResponseEvent);

        InputValidator inputValidator = new InputValidator(reserveResponseActionPatterns);

        Assertions.assertDoesNotThrow(() -> inputValidator.validateInput(reserveResponseEvent));
    }

    @Test
    void reserveResponseEventNegative() throws IOException {
        String reserveResponseEvent = new String(Objects.requireNonNull(this.getClass()
                        .getClassLoader()
                        .getResourceAsStream("reserve-response-event-negative.json"))
                .readAllBytes());
        log.info("Income negative reserve response '{}'", reserveResponseEvent);

        InputValidator inputValidator = new InputValidator(reserveResponseActionPatterns);

        Assertions.assertThrows(ValidationException.class, () -> inputValidator.validateInput(reserveResponseEvent));
    }
}