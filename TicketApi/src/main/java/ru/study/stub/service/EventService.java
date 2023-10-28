package ru.study.stub.service;

import ru.study.stub.model.Event;

import java.time.Duration;

public interface EventService {


    Duration getTimeToPay(Event event);
}
