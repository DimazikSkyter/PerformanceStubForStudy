package ru.nspk.performance.transactionshandler.payment;

import ru.nspk.performance.action.PaymentLinkAction;

import java.util.function.Consumer;

public interface PaymentClient {

    void createPaymentLinkInPaymentService(PaymentLinkAction paymentLinkAction, Consumer<String> callback);
}
