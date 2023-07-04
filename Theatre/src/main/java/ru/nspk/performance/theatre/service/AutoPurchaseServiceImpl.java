package ru.nspk.performance.theatre.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.nspk.performance.theatre.exception.SeatsAlreadySoldException;
import ru.nspk.performance.theatre.model.PurchaseResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class AutoPurchaseServiceImpl implements PurchaseService {

    private final ReserveCache reserveCache;

    @Override
    public PurchaseResponse purchase(long reserveId) {
        try {
            reserveCache.getReserve(reserveId).purchase();
            reserveCache.evictReserve(reserveId);
        } catch (SeatsAlreadySoldException e) {
            log.error("Failed to purchase with reserveId {} with exception\n", reserveId, e);
            return PurchaseResponse.builder().result(false).build();
        }
        return PurchaseResponse.builder().result(true).build();
    }
}
