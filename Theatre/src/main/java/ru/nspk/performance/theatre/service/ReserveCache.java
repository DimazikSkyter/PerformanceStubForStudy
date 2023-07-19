package ru.nspk.performance.theatre.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import ru.nspk.performance.theatre.model.Reserve;

public interface ReserveCache {

    @Cacheable(value = "reserves", unless = "#result==null")
    Reserve getReserve(long reserveId);

    @CachePut(value = "reserves", key = "#reserveId")
    Reserve putReserve(long reserveId, Reserve reserve);

    @CacheEvict(value = "reserves")
    void evictReserve(long reserveId);
}
