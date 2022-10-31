package ru.study.stub.service;

import ru.study.stub.model.Event;

import java.time.Duration;

public interface EventService {

    double findEventAndGetItPrice(Event event);

    Duration getTimeToPay(Event event);
}
