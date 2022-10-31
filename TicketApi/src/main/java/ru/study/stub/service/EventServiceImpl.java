package ru.study.stub.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.study.stub.exception.EventNotFoundException;
import ru.study.stub.model.Event;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {

    private Map<String, Double> events;

    @Override
    public double findEventAndGetItPrice(Event event) {
        double basePrice = Optional.ofNullable(events.get(event.getEventName()))
                .orElseThrow(() -> new EventNotFoundException(event.getEventName()));
        return basePrice * event.getEventLevel().getCoeff();
    }

    @Override
    public Duration getTimeToPay(Event event) {
        return Duration.of(10, ChronoUnit.MINUTES);
    }
}
