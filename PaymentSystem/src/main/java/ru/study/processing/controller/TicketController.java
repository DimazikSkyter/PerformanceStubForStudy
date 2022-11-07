package ru.study.processing.controller;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.study.processing.model.Ticket;
import ru.study.processing.service.Processing;
import ru.study.processing.service.TicketProcessingImpl;

import java.time.Instant;

@Slf4j
@RestController
@RequestMapping("/processing/ticket")
@RequiredArgsConstructor
public class TicketController {

    private final Processing ticketProcessing;

    @PutMapping()
    public ResponseEntity<String> newTicket(@RequestParam String uid, @RequestParam double price, @RequestParam Instant timestamp) {
        Ticket ticket = Ticket.builder()
                .uidToPay(uid)
                .price(price)
                .timeToPayLimit(timestamp)
                .build();
        try {
            ticketProcessing.addNewTicket(ticket);
            return ResponseEntity.ok().build();
        } catch (Exception ex) {
            log.error("Failed to register new ticket {}.", ticket, ex);
            return ResponseEntity.unprocessableEntity().build();
        }
    }
}
