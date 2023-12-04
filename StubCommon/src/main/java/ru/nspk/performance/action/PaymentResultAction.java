package ru.nspk.performance.action;

import lombok.Data;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Data
@SuperBuilder
@Jacksonized
public class PaymentResultAction extends Action {

    private boolean paymentSuccess;
    private String errorMessage;

    protected PaymentResultAction(ActionBuilder<?, ?> b) {
        super(b);
    }
}
