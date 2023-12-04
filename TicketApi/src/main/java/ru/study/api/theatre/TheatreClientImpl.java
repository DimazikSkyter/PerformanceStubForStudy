package ru.study.api.theatre;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import ru.study.api.dto.SeatResponse;
import ru.study.api.model.Event;
import ru.study.api.properties.TheatreProperty;

import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


@Slf4j
@Component
@RequiredArgsConstructor
//todo перенести theatre client в отдельную библиотеку
public class TheatreClientImpl implements TheatreClient {

    private final WebClient client;
    private final TheatreProperty theatreProperty;

    @Override
    public Set<String> events() throws ExecutionException, InterruptedException, TimeoutException {
        return client.get()
                .uri("/theatre/events")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Set<String>>() {
                })
                .retry(theatreProperty.getRetries())
                .toFuture()
                .get(theatreProperty.getClientTimeoutMs(), TimeUnit.MILLISECONDS);
    }

    @Override
    public SeatResponse seats(String event) throws ExecutionException, InterruptedException, TimeoutException {
        return client.get()
                .uri("/theatre/seats/" + event)
                .retrieve()
                .bodyToMono(SeatResponse.class)
                .toFuture()
                .get(theatreProperty.getClientTimeoutMs(), TimeUnit.MILLISECONDS);
    }

    @Override
    public Event info(String event) throws ExecutionException, InterruptedException, TimeoutException {
        return client.get()
                .uri("/theatre/events/" + event + "/info")
                .retrieve()
                .bodyToMono(Event.class)
                .filter(Event::isExists)
                .map(event1 -> {
                    event1.setName(event);
                    return event1;
                })
                .toFuture()
                .get(theatreProperty.getClientTimeoutMs(), TimeUnit.MILLISECONDS);
    }
}
