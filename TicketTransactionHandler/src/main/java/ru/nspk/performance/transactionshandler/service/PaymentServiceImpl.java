package ru.nspk.performance.transactionshandler.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.nspk.performance.transactionshandler.model.PaymentRequest;
import ru.nspk.performance.transactionshandler.properties.PaymentServiceProperties;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private static final String URI_PATH_PAYMENT_SERVICE = "/processing/payment/paymentLink";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient;
    private final PaymentServiceProperties paymentServiceProperties;

    @Override
    public CompletableFuture<HttpResponse<byte[]>> createPaymentLink(PaymentRequest paymentRequest) throws URISyntaxException, IOException, InterruptedException {
        HttpRequest httpRequest = HttpRequest
                .newBuilder()
                .uri(new URI(paymentServiceProperties.getPaymentServiceBaseAddress() + URI_PATH_PAYMENT_SERVICE))
                .header("INSTANCE_KEY", paymentServiceProperties.getInstanceKey())
                .header("REQUEST_ID", paymentRequest.requestId())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(paymentRequest.toPaymentDetailsDto())))
                .build();
        return httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofByteArray());
    }
}
