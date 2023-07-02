package ru.nspk.performance.theatre.service;


import ru.nspk.performance.theatre.model.ReserveResponse;

import java.util.List;
import java.util.Set;

public interface EventService {

    Set<String> events();

    Set<String> seats(String eventName);

    ReserveResponse reserve(String eventName, List<String> seats);

    void release(long reserveId);
}
