package ru.nspk.performance.theatre.service;

import ru.nspk.performance.theatre.dto.PurchaseResponse;

public interface PurchaseService {

    PurchaseResponse purchase(long reserveId, String requestId);
}
