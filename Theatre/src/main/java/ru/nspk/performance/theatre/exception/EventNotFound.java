package ru.nspk.performance.theatre.exception;

public class EventNotFound extends RuntimeException {

    public EventNotFound(String event) {
        super("Event " + event + " was not found.");
    }
}
