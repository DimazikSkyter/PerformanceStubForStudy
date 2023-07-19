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
import ru.nspk.performance.theatre.model.SeatStatus;
import ru.nspk.performance.theatre.properties.CacheProperties;
import ru.nspk.performance.theatre.service.ReserveCache;

import java.time.Instant;
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
        Map<String, SeatStatus> seats = new HashMap<>();
        seats.putAll(Map.of(
                "A1", SeatStatus.FREE,
                "A2", SeatStatus.FREE,
                "A3", SeatStatus.FREE,
                "B1", SeatStatus.FREE,
                "B2", SeatStatus.RESERVED,
                "B3", SeatStatus.RESERVED
        ));
        Event event = new Event("custom event", seats);
        reserveCache.putReserve(1L, new Reserve(event, Instant.now(), List.of("B2", "B3")));
        Cache reserves = cacheManager.getCache("reserves");
        Thread.sleep(2000);
        reserveCache.evictReserve(1L);
        Reserve reserve = reserveCache.getReserve(1L);
        log.info("Reserve is {}", reserve);
        Thread.sleep(3333);
        log.info(seats.toString(), seats);
    }
}

