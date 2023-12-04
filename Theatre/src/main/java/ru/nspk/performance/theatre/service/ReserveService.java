package ru.nspk.performance.theatre.service;

import ru.nspk.performance.theatre.dto.ReleaseResponse;
import ru.nspk.performance.theatre.dto.ReserveResponse;

import java.util.List;

public interface ReserveService {

    ReserveResponse reserve(String eventName, List<String> seats, String requestId);

    ReleaseResponse release(long reserveId);
}
