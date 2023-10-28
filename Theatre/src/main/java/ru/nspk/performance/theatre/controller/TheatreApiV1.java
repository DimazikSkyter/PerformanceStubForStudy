package ru.nspk.performance.theatre.controller;

import ru.nspk.performance.theatre.dto.*;

import java.util.List;

public interface TheatreApiV1 {

    EventResponse events();

    SeatDto seats(String event);

    ReserveResponse reserve(String event, List<String> seats);

    ReleaseResponse release(long reserveId);

    PurchaseResponse purchase(long reserveId);
}
