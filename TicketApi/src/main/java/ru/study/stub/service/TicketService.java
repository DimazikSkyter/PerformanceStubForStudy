package ru.study.stub.service;

import ru.study.stub.dto.TicketDto;
import ru.study.stub.model.TicketResponse;

public interface TicketService {

    TicketResponse createNewTicket(TicketDto ticketDto);
}
