package ru.nspk.performance.theatre.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.nspk.performance.theatre.dto.ReleaseResponse;
import ru.nspk.performance.theatre.dto.ReserveResponse;
import ru.nspk.performance.theatre.exception.EventNotFound;
import ru.nspk.performance.theatre.model.Event;
import ru.nspk.performance.theatre.model.Reserve;
import ru.nspk.performance.theatre.model.Seat;
import ru.nspk.performance.theatre.model.SeatStatus;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
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
        log.debug("Make new reserve for event {} and seats {}", eventName, seats);
        try {
            Event event = Optional.ofNullable(eventService.getEvents().get(eventName)).orElseThrow(() -> new EventNotFound(eventName));

            Set<Map.Entry<String, Seat>> nonFreeSeats;
            synchronized (event) {
                nonFreeSeats = seats.stream()
                        .map(seat -> Map.entry(seat, event.getSeats().get(seat)))
                        .filter(entry -> !entry.getValue().seatStatus().equals(SeatStatus.FREE))
                        .collect(Collectors.toSet());
                if (nonFreeSeats.isEmpty()) {
                    AtomicReference<Double> sum = new AtomicReference<>(0D);
                    seats.forEach(seat -> {
                        double price = event.getSeats().get(seat).price();
                        event.getSeats().put(seat, new Seat(SeatStatus.RESERVED, price));
                        sum.accumulateAndGet(price, Double::sum);
                    });
                    int reserveId = reserveSequence.getAndIncrement();
                    reserveCache.putReserve(reserveId, new Reserve(event, Instant.now(), seats, sum.get()));
                    return ReserveResponse.builder()
                            .reserveId(reserveId)
                            .nonFreeSeats(Set.of())
                            .reserveStarted(Instant.now())
                            .build();
                }
                return ReserveResponse.builder()
                        .reserveId(-1)
                        .nonFreeSeats(nonFreeSeats.stream().map(Map.Entry::getKey).collect(Collectors.toSet()))
                        .build();
            }
        } catch (EventNotFound e) {
            return ReserveResponse.builder()
                    .reserveId(-1)
                    .errorMessage("Event not found")
                    .build();
        } catch (Exception e) {
            return ReserveResponse.builder()
                    .reserveId(-1)
                    .errorMessage("Failed to make reserve")
                    .build();
        }
    }

    @Override
    public ReleaseResponse release(long reserveId) {
        try {
            Reserve reserve = reserveCache.getReserve(reserveId);
            if (reserve == null) {
                return ReleaseResponse.failed(reserveId, "Reserve not found");
            }
            reserve.getEvent().releaseAll(reserve.getSeats());
            return ReleaseResponse.success(reserveId);
        } catch (Exception e) {
            log.error("Failed to get value from reserve cache. ReserveId = {}", reserveId);
            return ReleaseResponse.failed(reserveId, "Failed to release. Please wait purchase.");
        }
    }
}
