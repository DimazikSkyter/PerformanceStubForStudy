package ru.nspk.performance.theatre.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.nspk.performance.theatre.exception.EventNotFound;
import ru.nspk.performance.theatre.model.Event;
import ru.nspk.performance.theatre.model.Reserve;
import ru.nspk.performance.theatre.model.ReserveResponse;
import ru.nspk.performance.theatre.model.SeatStatus;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReserveServiceWithTimeout implements ReserveService {

    private final ReserveCache reserveCache;
    private final EventService eventService;

    private AtomicInteger reserveSequence = new AtomicInteger();

    @Override
    public ReserveResponse reserve(String eventName, List<String> seats) {
        Event event = Optional.ofNullable(eventService.getEvents().get(eventName)).orElseThrow(() -> new EventNotFound(eventName));

        Set<Map.Entry<String, SeatStatus>> nonFreeSeats = Set.of();
        synchronized (event) {
            nonFreeSeats = seats.stream()
                    .map(seat -> Map.entry(seat, event.getSeats().get(seat)))
                    .filter(entry -> !entry.getValue().equals(SeatStatus.FREE))
                    .collect(Collectors.toSet());
            if (nonFreeSeats.isEmpty()) {
                seats.forEach(seat -> event.getSeats().put(seat, SeatStatus.RESERVED));
                int reserveId = reserveSequence.getAndIncrement();
                reserveCache.putReserve(reserveId, new Reserve(event, Instant.now(), seats));
                return ReserveResponse.builder()
                        .reserveId(reserveId)
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
        try {
            Reserve reserve = reserveCache.getReserve(reserveId);
            reserve.getEvent().releaseAll(reserve.getSeats());
        } catch (Exception e) {
            log.error("Failed to get value from reserve cache. ReserveId = {}", reserveId);
        }
    }
}
