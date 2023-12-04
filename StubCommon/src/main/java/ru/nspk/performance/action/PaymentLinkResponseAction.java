package ru.nspk.performance.action;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;

@Data
@SuperBuilder
@Jacksonized
@ToString(exclude = {"paymentLinkBytes"})
public class PaymentLinkResponseAction extends Action {

    private String status;
    private byte[] paymentLinkBytes;
    private Instant paymentLinkCreated;
    private long timeToPayMs;
    private String requestId;
    private String paymentLinkErrorMessage;

    public PaymentLinkResponseAction(ActionBuilder<?, ?> b,
                                     String status,
                                     byte[] paymentLinkBytes,
                                     String  requestId,
                                     String paymentLinkErrorMessage,
                                     Instant paymentLinkCreated,
                                     long timeToPayMs) {
        super(b);
        super.setTransactionId(transactionId);
        this.requestId = requestId;
        this.status = status;
        this.paymentLinkBytes = paymentLinkBytes;
        this.paymentLinkErrorMessage = paymentLinkErrorMessage;
        this.paymentLinkCreated = paymentLinkCreated;
        this.timeToPayMs = timeToPayMs;
    }
}
