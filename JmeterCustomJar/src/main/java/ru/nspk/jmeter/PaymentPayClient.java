package ru.nspk.jmeter;

import reactor.core.publisher.Mono;
import reactor.netty.ByteBufMono;
import reactor.netty.http.client.HttpClient;

public class PaymentPayClient {

    private final String url;

    public PaymentPayClient(String url) {
        this.url = url;
    }

    public void pay(String requestId) {
        String body = String.format("{\"secretKey\":123,\"secretPaymentIdentification\":\"%s\"}", requestId);
        HttpClient.create()
                .baseUrl(url)
                .headers(entries -> entries.set("Content-Type", "application/json"))
                .post()
                .send(ByteBufMono.fromString(Mono.just(body)))
                .responseSingle((res, content) -> Mono.just(res.status().code()))
                .block();
    }
}
