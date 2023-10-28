package ru.study.stub.service;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.study.stub.exception.EventNotFoundException;
import ru.study.stub.model.Event;
import ru.study.stub.theatre.TheatreClient;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private AtomicReference<Map<String, Double>> events = new AtomicReference<>(new HashMap<>());
    private final TheatreClient theatreClient;


    @Scheduled(cron = "*/5 * * * *")
    public void updateEvents() {

    }

    @Override
    public Duration getTimeToPay(Event event) {
        return Duration.of(10, ChronoUnit.MINUTES);
    }
}
