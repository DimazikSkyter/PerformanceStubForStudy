package ru.nspk.performance.theatre.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.nspk.performance.theatre.entity.Purchase;
import ru.nspk.performance.theatre.dto.PurchaseResponse;
import ru.nspk.performance.theatre.model.Reserve;
import ru.nspk.performance.theatre.repository.PurchaseRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class AutoPurchaseServiceImpl implements PurchaseService {

    private final ReserveCache reserveCache;
    private final PurchaseRepository purchaseRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public PurchaseResponse purchase(long reserveId, String requestId) {
        try {
            Reserve reserve = reserveCache.getReserve(reserveId);
            reserve.purchase();
            reserveCache.evictReserve(reserveId);
            purchaseRepository.save(Purchase.builder()
                            .reserveId(reserveId)
                            .event(reserve.getEvent().getName())
                            .sum(reserve.getSum())
                            .seats(objectMapper.writeValueAsString(reserve.getSeats()))
                            .eventDate(reserve.getEvent().getEventDate())
                    .build());
        } catch (Exception e) {
            log.error("Failed to purchase with reserveId {} with exception\n", reserveId, e);
            return PurchaseResponse.builder().requestId(requestId).result(false).build();
        }
        return PurchaseResponse.builder().requestId(requestId).result(true).build();
    }
}
