package ru.nspk.performance.theatre.service;


import ru.nspk.performance.theatre.dto.CreateEventRequest;
import ru.nspk.performance.theatre.dto.EventDto;
import ru.nspk.performance.theatre.dto.SeatResponse;
import ru.nspk.performance.theatre.model.Event;

import java.util.Map;
import java.util.Set;

public interface EventService {

    Map<String, Event> getEvents();
    Set<String> eventNames();

    SeatResponse seats(String eventName);

    EventDto eventInfo(String eventName);

    EventDto createNewEvent(CreateEventRequest createEventRequest);
}
