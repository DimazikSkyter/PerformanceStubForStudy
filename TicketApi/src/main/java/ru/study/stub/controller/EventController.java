package ru.study.stub.controller;

import org.springframework.http.ResponseEntity;
import ru.study.stub.dto.SeatsDto;
import ru.study.stub.model.Event;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class EventController implements EventApi {
    @Override
    public ResponseEntity<List<Event>> eventsOfMerchantAndDate(String merchant, Date date) {
        return null;
    }

    @Override
    public ResponseEntity<SeatsDto> seatsOfEvent(String eventName) {
        return null;
    }
}
