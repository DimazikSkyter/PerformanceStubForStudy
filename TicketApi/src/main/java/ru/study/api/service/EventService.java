package ru.study.api.service;

import ru.study.api.dto.EventResponse;
import ru.study.api.dto.SeatResponse;
import ru.study.api.model.Event;

import java.time.Duration;
import java.util.Date;

public interface EventService {


    Duration getTimeToPay(Event event);

    Event getEventByName(String eventName);

    EventResponse getEventsByMerchantAndDate(String merchant, Date date);

    SeatResponse getSeatsFromEvent(String eventName);
}
