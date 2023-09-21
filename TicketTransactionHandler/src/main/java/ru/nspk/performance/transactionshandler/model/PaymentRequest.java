package ru.nspk.performance.transactionshandler.model;

import lombok.Data;

public record PaymentRequest(String requestId, long transactionId) {

    public PaymentDetailsDto toPaymentDetailsDto() {
        return null;
    }
}
