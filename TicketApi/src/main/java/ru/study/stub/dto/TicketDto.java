package ru.study.stub.dto;

import lombok.Data;
import ru.study.stub.model.Event;
import ru.study.stub.model.Person;

@Data
public class TicketDto {

    private final Event event;
    private final Person person;
}
