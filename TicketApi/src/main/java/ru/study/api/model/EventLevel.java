package ru.study.api.model;

import lombok.Getter;
import ru.study.stub.proto.Ticket;

public enum EventLevel {

    LOW(0.5, Ticket.EventType.LOW),
    MIDDLE(1D, Ticket.EventType.MIDDLE),
    HIGH(2D, Ticket.EventType.HIGH);

    @Getter
    private final double coeff;
    @Getter
    private Ticket.EventType eventType;

    EventLevel(double coeff, Ticket.EventType eventType) {
        this.coeff = coeff;
        this.eventType = eventType;
    }
}
