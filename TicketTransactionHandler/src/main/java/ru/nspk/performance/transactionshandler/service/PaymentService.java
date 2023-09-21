package ru.nspk.performance.transactionshandler.service;

import ru.nspk.performance.transactionshandler.model.PaymentRequest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public interface PaymentService {

    CompletableFuture<HttpResponse<byte[]>> createPaymentLink(PaymentRequest paymentRequest) throws URISyntaxException, IOException, InterruptedException;
}
