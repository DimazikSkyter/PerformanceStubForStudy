package ru.nspk.performance.theatre.service;

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

    private final Map<String, Event> events;
    private long reserveSequence = 0;

    @Override
    public Set<String> events() {
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

    @Override
    public ReserveResponse reserve(String eventName, List<String> seats) {
        Event event = Optional.ofNullable(events.get(eventName)).orElseThrow(() -> new EventNotFound(eventName));

        Set<Map.Entry<String, SeatStatus>> nonFreeSeats = Set.of();
        synchronized (event) {
             nonFreeSeats = seats.stream()
                    .map(seat -> Map.entry(seat, event.getSeats().get(seat)))
                    .filter(entry -> !entry.getValue().equals(SeatStatus.FREE))
                    .collect(Collectors.toSet());
            if (nonFreeSeats.isEmpty()) {
                seats.forEach(seat -> event.getSeats().put(seat, SeatStatus.RESERVED));
                return ReserveResponse.builder()
                        .reserveId(reserveSequence++)
                        .nonFreeSeats(Set.of())
                        .reserveStarted(Instant.now())
                        .build();
            }
        }
        return ReserveResponse.builder()
                .reserveId(-1)
                .nonFreeSeats(nonFreeSeats.stream().map(Map.Entry::getKey).collect(Collectors.toSet()))
                .build();
    }

    @Override
    public void release(long reserveId) {

    }

    //todo автоматическое снятие резерва через 5 минут
}
