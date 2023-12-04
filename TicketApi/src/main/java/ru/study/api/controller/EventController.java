package ru.study.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.study.api.dto.EventResponse;
import ru.study.api.dto.SeatResponse;
import ru.study.api.service.EventService;

import java.util.Date;

@RequiredArgsConstructor
@RestController
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;

    @GetMapping("/list")
    public EventResponse eventsOfMerchantAndDate(@RequestParam(required = false) String merchant,
                                                 @RequestParam @DateTimeFormat(pattern="yyyy-MM-dd") Date date) {
        return eventService.getEventsByMerchantAndDate(merchant, date);
    }

    @GetMapping("/{event_name}/seats")
    public SeatResponse seatsOfEvent(@PathVariable("event_name") String eventName) {
        return eventService.getSeatsFromEvent(eventName);
    }
}
