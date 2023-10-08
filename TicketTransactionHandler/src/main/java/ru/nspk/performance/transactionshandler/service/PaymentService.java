package ru.nspk.performance.transactionshandler.service;

import ru.nspk.performance.transactionshandler.model.PaymentRequest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public interface PaymentService {

    CompletableFuture<byte[]> createPaymentLink(PaymentRequest paymentRequest) throws ExecutionException, InterruptedException, TimeoutException;
}
