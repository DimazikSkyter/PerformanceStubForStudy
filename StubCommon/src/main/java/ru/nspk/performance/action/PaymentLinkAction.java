package ru.nspk.performance.action;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Data
@SuperBuilder
@Jacksonized
public class PaymentLinkAction extends Action {

    private @NonNull String requestId;
    private String purpose;
    private String account;
    private double amount;

    public PaymentLinkAction(ActionBuilder<?, ?> b,
                             @NonNull String requestId,
                             @NonNull String purpose,
                             double amount,
                             Long transactionId) {
        super(b);
        super.setTransactionId(transactionId);
        this.requestId = requestId;
        this.purpose = purpose;
        this.amount = amount;
    }
}
