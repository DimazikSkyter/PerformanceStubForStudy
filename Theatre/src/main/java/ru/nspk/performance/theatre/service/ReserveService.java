package ru.nspk.performance.theatre.service;

import ru.nspk.performance.theatre.model.ReserveResponse;

import java.util.List;

public interface ReserveService {

    ReserveResponse reserve(String eventName, List<String> seats);

    void release(long reserveId);
}
