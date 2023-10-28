package ru.study.stub.controller;

import org.springframework.http.ResponseEntity;
import ru.study.stub.dto.SeatsDto;
import ru.study.stub.model.Event;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface EventApi {

    ResponseEntity eventsOfMerchantAndDate(String merchant, Date date);

    ResponseEntity<SeatsDto> seatsOfEvent(String eventName);
}
