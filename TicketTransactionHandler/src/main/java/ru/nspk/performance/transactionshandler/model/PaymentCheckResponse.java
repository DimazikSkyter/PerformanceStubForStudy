package ru.nspk.performance.transactionshandler.model;


import lombok.Data;

@Data
public class PaymentCheckResponse {

    private long requestId;
    private boolean paymentStatusSuccess;
}
