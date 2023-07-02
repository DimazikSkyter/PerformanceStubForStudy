package ru.nspk.performance.theatre.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.nspk.performance.theatre.model.PurchaseResponse;
import ru.nspk.performance.theatre.model.ReserveResponse;
import ru.nspk.performance.theatre.service.EventService;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/theatre")
@RequiredArgsConstructor
public class TheatreController {

    private final EventService eventService;

    @GetMapping("/events")
    public Set<String> events() {
        return eventService.events();
    }

    @GetMapping("/seats/{event}")
    public Set<String> seats(@PathVariable String event) {
        return eventService.seats(event);
    }

    @PostMapping("/reserve")
    public ReserveResponse reserve(@RequestParam String event, @RequestParam List<String> seats) {
        return eventService.reserve(event, seats);
    }

    @PostMapping("/release")
    public void release(@RequestParam long reserveId) {
        eventService.release(reserveId);
    }

    @PostMapping("/purchase")
    public PurchaseResponse purchase() {
        return null;
    }
}
