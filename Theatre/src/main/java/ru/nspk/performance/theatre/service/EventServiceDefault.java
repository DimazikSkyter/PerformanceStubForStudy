package ru.nspk.performance.theatre.service;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.nspk.performance.theatre.exception.EventNotFound;
import ru.nspk.performance.theatre.model.Event;
import ru.nspk.performance.theatre.model.ReserveResponse;
import ru.nspk.performance.theatre.model.SeatStatus;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventServiceDefault implements EventService {

    @Getter
    private final Map<String, Event> events;

    @Override
    public Set<String> eventNames() {
        return events.keySet();
    }

    @Override
    public Set<String> seats(String eventName) {
        return Optional.ofNullable(
                        events.get(eventName)).orElseThrow(() -> new EventNotFound(eventName))
                .getSeats()
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue() != null && entry.getValue().equals(SeatStatus.FREE))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }
}
