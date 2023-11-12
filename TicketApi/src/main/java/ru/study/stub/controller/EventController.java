package ru.study.stub.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.study.stub.dto.EventResponse;
import ru.study.stub.dto.SeatResponse;
import ru.study.stub.service.EventService;

import java.util.Date;

@RequiredArgsConstructor
@RestController
@RequestMapping("/events")
public class EventController {

    private EventService eventService;

    @GetMapping("/list")
    public EventResponse eventsOfMerchantAndDate(@RequestParam String merchant,
                                                 @RequestParam Date date) {
        return eventService.getEventsByMerchantAndDate(merchant, date);
    }

    @GetMapping("/{event_name}/seats")
    public SeatResponse seatsOfEvent(@PathVariable("event_name") String eventName) {
        return eventService.getSeatsFromEvent(eventName);
    }
}
