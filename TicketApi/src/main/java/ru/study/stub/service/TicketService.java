package ru.study.stub.service;

import ru.study.stub.dto.TicketDto;
import ru.study.stub.model.Ticket;

public interface TicketService {

    Ticket createNewTicket(TicketDto ticketDto);
}
