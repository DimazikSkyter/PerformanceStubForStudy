package ru.nspk.performance.theatre;


import lombok.extern.slf4j.Slf4j;
import org.cache2k.event.CacheEntryExpiredListener;
import org.cache2k.event.CacheEntryRemovedListener;
import org.cache2k.extra.spring.SpringCache2kCacheManager;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.nspk.performance.theatre.model.Event;
import ru.nspk.performance.theatre.model.Reserve;
import ru.nspk.performance.theatre.properties.CacheProperties;
import ru.nspk.performance.theatre.service.ReserveCache;

@Configuration
@EnableCaching
@Slf4j
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(CacheProperties cacheProperties) {
        return new SpringCache2kCacheManager()
                .defaultSetup(b -> b.entryCapacity(100000))
                .addCache(b -> b.name("reserves")
                        .keyType(Long.class)
                        .expireAfterWrite(cacheProperties.getTimeout())
                        .addAsyncListener((CacheEntryRemovedListener) (cache, entry) -> {
                            log.info("Handle remove the reserve with key = {}, value = {}", entry.getKey(), entry.getValue());
                            Reserve reserve = (Reserve) entry.getValue();
                            Event event = reserve.getEvent();
                            event.releaseAll(reserve.getSeats());
                        })
                        .addAsyncListener((CacheEntryExpiredListener) (cache, entry) -> {
                            log.info("Handle expire the reserve with key = {}, value = {}", entry.getKey(), entry.getValue());
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
            public Reserve putReserve(long reserveId, Reserve reserve) {
                return reserve;
            }

            @Override
            public void evictReserve(long reserveId) {
            }
        };
    }
}
