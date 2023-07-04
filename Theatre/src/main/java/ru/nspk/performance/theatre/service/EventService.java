package ru.nspk.performance.theatre.service;


import ru.nspk.performance.theatre.model.Event;

import java.util.Map;
import java.util.Set;

public interface EventService {

    Map<String, Event> getEvents();
    Set<String> eventNames();

    Set<String> seats(String eventName);
}