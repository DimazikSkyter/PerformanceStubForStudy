package ru.study.api.dto;

import ru.nspk.performance.keyvaluestorage.model.Person;

public record TicketDto(Person person, SeatDto seat) {
}
