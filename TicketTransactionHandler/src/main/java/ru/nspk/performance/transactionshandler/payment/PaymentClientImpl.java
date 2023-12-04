package ru.nspk.performance.transactionshandler.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.http.HttpClientTransportOverHTTP;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.JettyClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.nspk.performance.action.PaymentLinkAction;
import ru.nspk.performance.transactionshandler.properties.PaymentClientProperties;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;


@Slf4j
public class PaymentClientImpl implements PaymentClient {

    private final WebClient client;
    private final PaymentClientProperties paymentClientProperties;


    public PaymentClientImpl(String paymentServiceBaseUrl, PaymentClientProperties paymentClientProperties) {
        client = WebClient.builder().baseUrl(paymentServiceBaseUrl).clientConnector(
                new JettyClientHttpConnector(new HttpClient(new HttpClientTransportOverHTTP()))
        ).build();
        this.paymentClientProperties = paymentClientProperties;
    }

    @Override
    public void createPaymentLinkInPaymentService(PaymentLinkAction paymentLinkAction, Consumer<String> callback) {
        log.debug("Calling theatre client with request id {}", paymentLinkAction.getRequestId());
        Map<String, String> body = new HashMap<>();
        body.put("purpose", paymentLinkAction.getPurpose());
        body.put("amount", String.valueOf(paymentLinkAction.getAmount()));
        body.put("account", String.valueOf(paymentLinkAction.getAccount()));
        client.post()
                .uri(uriBuilder -> uriBuilder.path("/processing/payment/payment_link").build())
                .contentType(MediaType.APPLICATION_JSON)
                .header("REQUEST_ID", paymentLinkAction.getRequestId())
                .body(Mono.just(body), Map.class)
                .retrieve()
                .bodyToMono(String.class)
                .toFuture()
                .thenAccept(callback)
                .orTimeout(paymentClientProperties.getTimeout(), TimeUnit.MILLISECONDS);
    }
}
