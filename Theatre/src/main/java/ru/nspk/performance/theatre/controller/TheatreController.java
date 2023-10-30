package ru.nspk.performance.theatre.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.nspk.performance.theatre.dto.*;
import ru.nspk.performance.theatre.service.EventService;
import ru.nspk.performance.theatre.service.PurchaseService;
import ru.nspk.performance.theatre.service.ReserveService;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/theatre")
@RequiredArgsConstructor
public class TheatreController {

    private final EventService eventService;
    private final ReserveService reserveService;
    private final PurchaseService purchaseService;

    @GetMapping("/events")
    public Set<String> events() {
        return eventService.eventNames();
    }

    @GetMapping("/seats/{event}")
    public SeatResponse seats(@PathVariable String event) {
        return eventService.seats(event);
    }

    @PostMapping("/reserve")
    public ReserveResponse reserve(@RequestParam(name = "event") String event, @RequestParam(name = "seat") List<String> seats, @RequestHeader(name = "XREQUEST_ID") long requestId) {
        return reserveService.reserve(event, seats, requestId);
    }

    @PostMapping("/release")
    public ReleaseResponse release(@RequestParam(name = "reserve_id") long reserveId) {
        return reserveService.release(reserveId);
    }

    @PostMapping("/purchase")
    public PurchaseResponse purchase(@RequestParam(name = "reserve_id") long reserveId) {
        return purchaseService.purchase(reserveId);
    }
}
