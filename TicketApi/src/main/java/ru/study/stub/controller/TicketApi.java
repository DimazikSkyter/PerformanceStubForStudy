package ru.study.stub.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import ru.study.stub.dto.TicketDto;

public interface TicketApi {

    ResponseEntity buyTickets(TicketDto ticketDto);

    ResponseEntity checkTicketStatus(String uid);

    ResponseEntity cancelTicket(@PathVariable String uid);
}
