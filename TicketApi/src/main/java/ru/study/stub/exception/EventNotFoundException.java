package ru.study.stub.exception;

import lombok.Getter;

public class EventNotFoundException extends NullPointerException {

    @Getter
    private String eventName;

    public EventNotFoundException(String eventName) {
        super("Event " + eventName + " not found");
        this.eventName = eventName;
    }
}
