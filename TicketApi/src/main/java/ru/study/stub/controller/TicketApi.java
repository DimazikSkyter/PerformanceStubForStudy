package ru.study.stub.controller;

import org.springframework.http.ResponseEntity;
import ru.study.stub.dto.Subscriber;
import ru.study.stub.dto.TicketDto;

public interface TicketApi {

    ResponseEntity createNewTicket(TicketDto ticketDto);

    ResponseEntity checkTicketStatus(String uid);

    ResponseEntity subscribeToPush(Subscriber subscriber);

    ResponseEntity unSubscribeToPush(Subscriber subscriber);
}
