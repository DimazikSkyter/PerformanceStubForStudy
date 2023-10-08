package ru.nspk.performance.transactionshandler.service;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.http.HttpClientTransportOverHTTP;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.reactive.JettyClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import ru.nspk.performance.transactionshandler.model.PaymentDetailsDto;
import ru.nspk.performance.transactionshandler.model.PaymentRequest;
import ru.nspk.performance.transactionshandler.properties.PaymentServiceProperties;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentServiceProperties paymentServiceProperties;
    private final WebClient client;
    public PaymentServiceImpl(PaymentServiceProperties paymentServiceProperties) {
        this.paymentServiceProperties = paymentServiceProperties;
        this.client = WebClient.builder()
                .baseUrl(paymentServiceProperties.getPaymentServiceBaseAddress())
                .clientConnector(
                        new JettyClientHttpConnector(new HttpClient(new HttpClientTransportOverHTTP()))
                ).build();

    }

    @Override
    public CompletableFuture<byte[]> createPaymentLink(PaymentRequest paymentRequest) throws ExecutionException, InterruptedException, TimeoutException {
        return client.post()
                .uri(paymentServiceProperties.getPaymentServicePath())
                .header("INSTANCE_KEY", paymentServiceProperties.getInstanceKey())
                .header("REQUEST_ID", paymentRequest.requestId())
                .header("Content-Type", "application/json")
                .body(paymentRequest.toPaymentDetailsDto(), PaymentDetailsDto.class)
                .retrieve().bodyToMono(new ParameterizedTypeReference<byte[]>() {
                }).toFuture();
    }
}
