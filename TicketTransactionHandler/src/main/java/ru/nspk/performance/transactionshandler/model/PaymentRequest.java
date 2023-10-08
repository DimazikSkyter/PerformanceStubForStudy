package ru.nspk.performance.transactionshandler.model;

import lombok.Data;
import lombok.NonNull;

public record PaymentRequest(String requestId, long transactionId) {

    public @NonNull PaymentDetailsDto toPaymentDetailsDto() {
        return null;
    }
}
