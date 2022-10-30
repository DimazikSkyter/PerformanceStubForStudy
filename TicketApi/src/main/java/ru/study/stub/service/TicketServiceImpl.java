package ru.study.stub.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.study.stub.dto.TicketDto;
import ru.study.stub.model.Ticket;

@Service
@AllArgsConstructor
@Slf4j
public class TicketServiceImpl implements TicketService {

    private final EventService eventService;

    @Override
    public Ticket createNewTicket(TicketDto ticketDto) {
        return null;
    }
}
