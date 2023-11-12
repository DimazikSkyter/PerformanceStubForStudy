package ru.study.stub.dto;

import ru.study.stub.model.Person;

public record TicketDto(Person person, SeatDto seat) {
}
