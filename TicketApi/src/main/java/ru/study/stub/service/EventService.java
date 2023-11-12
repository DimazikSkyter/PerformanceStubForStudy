package ru.study.stub.service;

import ru.study.stub.dto.EventResponse;
import ru.study.stub.dto.SeatResponse;
import ru.study.stub.model.Event;

import java.time.Duration;
import java.util.Date;

public interface EventService {


    Duration getTimeToPay(Event event);

    EventResponse getEventsByMerchantAndDate(String merchant, Date date);

    SeatResponse getSeatsFromEvent(String eventName);
}
