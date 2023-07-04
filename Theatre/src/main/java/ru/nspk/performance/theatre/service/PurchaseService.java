package ru.nspk.performance.theatre.service;

import ru.nspk.performance.theatre.model.PurchaseResponse;

public interface PurchaseService {

    PurchaseResponse purchase(long reserveId);
}
