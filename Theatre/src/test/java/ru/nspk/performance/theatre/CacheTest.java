package ru.nspk.performance.theatre;


import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.nspk.performance.theatre.model.Event;
import ru.nspk.performance.theatre.model.Reserve;
import ru.nspk.performance.theatre.model.Seat;
import ru.nspk.performance.theatre.model.SeatStatus;
import ru.nspk.performance.theatre.properties.CacheProperties;
import ru.nspk.performance.theatre.service.ReserveCache;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ActiveProfiles("cache")
@ExtendWith(SpringExtension.class)
@SpringBootTest
@Slf4j
public class CacheTest {

    @Autowired
    ReserveCache reserveCache;
    @Autowired
    private CacheProperties cacheProperties;
    @Autowired
    private CacheManager cacheManager;

    @Test
    void test() throws InterruptedException {
        Map<String, Seat> seats = new HashMap<>();
        seats.putAll(Map.of(
                "A1", new Seat(SeatStatus.FREE, 31.1),
                "A2", new Seat(SeatStatus.FREE, 31.1),
                "A3", new Seat(SeatStatus.FREE, 31.1),
                "B1", new Seat(SeatStatus.FREE, 31.1),
                "B2", new Seat(SeatStatus.FREE, 31.2),
                "B3", new Seat(SeatStatus.FREE, 31.2)
        ));
        Event event = new Event("custom event", Date.from(Instant.now()), seats, "merchant", "type");
        reserveCache.putReserve(1L, new Reserve(event, Instant.now(), List.of("B2", "B3"), seats.values().stream().map(Seat::price).reduce(Double::sum).get()));
        Cache reserves = cacheManager.getCache("reserves");
        Thread.sleep(2000);
        reserveCache.evictReserve(1L);
        Reserve reserve = reserveCache.getReserve(1L);
        log.info("Reserve is {}", reserve);
        Thread.sleep(3333);
        log.info(seats.toString(), seats);
    }
}

