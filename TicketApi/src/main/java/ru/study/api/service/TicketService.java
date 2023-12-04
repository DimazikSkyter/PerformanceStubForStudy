package ru.study.api.service;

import ru.study.api.dto.TicketCheckResponse;
import ru.study.api.dto.TicketPurchaseRequest;
import ru.study.api.dto.TicketPurchaseResponse;

public interface TicketService {

    TicketPurchaseResponse createNewTicket(TicketPurchaseRequest tickets);

    TicketCheckResponse checkTicket(String uid);
}
