package ru.nspk.performance.transactionshandler.theatreclient;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.http.HttpClientTransportOverHTTP;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.reactive.JettyClientHttpConnector;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.nspk.performance.action.NotifyTheatreAction;
import ru.nspk.performance.transactionshandler.properties.TheatreClientProperties;

import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;


@Slf4j
public class TheatreClientImpl implements TheatreClient {

    private final WebClient client;
    private final TheatreClientProperties theatreClientProperties;

    public TheatreClientImpl(String theatreBaseUrl, TheatreClientProperties theatreClientProperties) {
        client = WebClient.builder().baseUrl(theatreBaseUrl).clientConnector(
                new JettyClientHttpConnector(new HttpClient(new HttpClientTransportOverHTTP()))
        ).build();
        this.theatreClientProperties = theatreClientProperties;
    }

    @Override
    public Set<String> events() throws ExecutionException, InterruptedException, TimeoutException {
        return client.get()
                .uri("/theatre/events")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Set<String>>() {
                })
                .toFuture()
                .get(theatreClientProperties.getTimeout(), TimeUnit.MILLISECONDS);
    }

    @Override
    public Set<String> seats(String event) throws ExecutionException, InterruptedException, TimeoutException {
        return client.get()
                .uri("/theatre/seats/" + event)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Set<String>>() {
                })
                .toFuture()
                .get(theatreClientProperties.getTimeout(), TimeUnit.MILLISECONDS);
    }

    @Override
    public void reserve(String requestId, String event, String seats, Consumer<String> callback) {
        log.debug("Calling theatre client with request id {}", requestId);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("event", event);
        body.add("seat", seats);
        client.post()
                .uri(uriBuilder -> uriBuilder.path("/theatre/reserve").build())
                .header("REQUEST_ID", requestId)
                .body(BodyInserters.fromFormData(body))
                .retrieve()
                .bodyToMono(String.class)
                .toFuture()
                .thenAccept(callback)
                .orTimeout(theatreClientProperties.getTimeout(), TimeUnit.MILLISECONDS);
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
    public void purchase(NotifyTheatreAction notifyTheatreAction, Consumer<String> callback) throws ExecutionException, InterruptedException, TimeoutException {
        //todo сделать политику ретрая если в театр пока не пришел положительный ответ
        client.post()
                .uri(uriBuilder -> uriBuilder.path("/theatre/purchase")
                        .queryParam("reserve_id", notifyTheatreAction.getReserveId()).build())
                .header("REQUEST_ID", notifyTheatreAction.getRequestId())
                .retrieve()
                .bodyToMono(String.class)
                .toFuture()
                .thenAccept(callback)
                .get(theatreClientProperties.getPurchaseTimeout(), TimeUnit.MILLISECONDS);
    }
}
