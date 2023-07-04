package ru.nspk.performance.theatre;


import org.cache2k.event.CacheEntryRemovedListener;
import org.cache2k.extra.spring.SpringCache2kCacheManager;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.nspk.performance.theatre.model.Event;
import ru.nspk.performance.theatre.model.Reserve;
import ru.nspk.performance.theatre.model.SeatStatus;
import ru.nspk.performance.theatre.properties.CacheProperties;
import ru.nspk.performance.theatre.service.ReserveCache;

import java.util.Map;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(CacheProperties cacheProperties) {
        return new SpringCache2kCacheManager()
                .defaultSetup(b -> b.entryCapacity(100000))
                .addCache(b -> b.name("reserves")
                        .expireAfterWrite(cacheProperties.getTimeout())
                        .addAsyncListener((CacheEntryRemovedListener) (cache, entry) -> {
                            Reserve reserve = (Reserve) entry.getValue();
                                    Event event = reserve.getEvent();
                                    event.releaseAll(reserve.getSeats());
                        }))
                ;
    }

    @Bean
    public ReserveCache reserveCache() {
        return new ReserveCache() {
            @Override
            public Reserve getReserve(long reserveId) {
                return null;
            }

            @Override
            public void putReserve(long reserveId, Reserve reserve) {
            }

            @Override
            public void evictReserve(long reserveId) {
            }
        };
    }
}
