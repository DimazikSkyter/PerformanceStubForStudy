package ru.study.stub.service;

import ru.study.stub.dto.TicketDto;
import ru.study.stub.dto.TicketPurchaseRequest;
import ru.study.stub.dto.TicketPurchaseResponse;
import ru.study.stub.entity.TicketStatus;

import java.util.List;

public interface TicketService {

    TicketPurchaseResponse createNewTicket(TicketPurchaseRequest tickets);

    TicketStatus checkTicket(String uid);
}
