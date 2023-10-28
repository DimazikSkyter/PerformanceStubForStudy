package ru.nspk.performance.transactionshandler.theatreclient;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.protocol.types.Field;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.http.HttpClientTransportOverHTTP;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.reactive.JettyClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.nspk.performance.theatre.model.PurchaseResponse;
import ru.nspk.performance.theatre.model.ReserveResponse;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;


@Slf4j
public class TheatreClientImpl implements TheatreClient {

    private final WebClient client;

    public TheatreClientImpl(String theatreBaseUrl) {
        client = WebClient.builder().baseUrl(theatreBaseUrl).clientConnector(
                new JettyClientHttpConnector(new HttpClient(new HttpClientTransportOverHTTP()))
        ).build();
    }

    @Override
    public Set<String> events() throws ExecutionException, InterruptedException, TimeoutException {
        return client.get()
                .uri("/theatre/events")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Set<String>>() {})
                .toFuture()
                .get(1, TimeUnit.MILLISECONDS);
    }

    @Override
    public Set<String> seats(String event) throws ExecutionException, InterruptedException, TimeoutException {
        return client.get()
                .uri("/theatre/seats/" + event)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Set<String>>() {})
                .toFuture()
                .get(1, TimeUnit.MILLISECONDS);
    }

    @Override
    public void reserve(String requestId, long event, List<String> seats, Consumer<String> callback) throws ExecutionException, InterruptedException, TimeoutException {
        client.post()
                .uri(uriBuilder -> uriBuilder.path("/theatre/reserve")
                        .queryParam("event", event)
                        .queryParam("seat", String.join(",", seats)).build())
                .header("REQUEST_ID", requestId)
                .retrieve()
                .bodyToMono(String.class)
                .toFuture()
                .thenAccept(callback)
                .orTimeout(1, TimeUnit.MILLISECONDS);
    }

    @Override
    public void release(long reserveId) {
        client.post()
                .uri(uriBuilder -> uriBuilder.path("/theatre/release")
                        .queryParam("reserve_id", reserveId).build())
                .retrieve()
                .onStatus(httpStatusCode ->
                                HttpStatusCode.valueOf(httpStatusCode.value()).is2xxSuccessful(),
                        clientResponse ->
                                Mono.create(throwableMonoSink ->
                                        log.info("Reserve {} successfully release", reserveId)));
        //todo сделать обработку негативного ответа
    }

    @Override
    public PurchaseResponse purchase(long reserveId) throws ExecutionException, InterruptedException, TimeoutException {
        //todo сделать политику ретрая если в театр пока не пришел положительный ответ
        return client.get()
                .uri(uriBuilder -> uriBuilder.path("/theatre/purchase")
                        .queryParam("reserve_id", reserveId).build())
                .retrieve()
                .bodyToMono(PurchaseResponse.class)
                .toFuture()
                .get(1, TimeUnit.MILLISECONDS);
    }
}