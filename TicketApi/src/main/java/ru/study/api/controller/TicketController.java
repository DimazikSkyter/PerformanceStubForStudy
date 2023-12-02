package ru.study.api.controller;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.web.bind.annotation.*;
import ru.study.api.dto.TicketCheckResponse;
import ru.study.api.dto.TicketPurchaseRequest;
import ru.study.api.dto.TicketPurchaseResponse;
import ru.study.api.service.TicketService;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/storage/api")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PutMapping("/ticket/purchase")
    public TicketPurchaseResponse buyTickets(@RequestBody TicketPurchaseRequest request, @Header(required = false) String uniqueId) { //header обрабатывается фильтром
        return ticketService.createNewTicket(request);
    }

    @GetMapping("/ticket/status/{uid}")
    public TicketCheckResponse checkTicketStatus(@PathVariable("uid") String uid) {
        return ticketService.checkTicket(uid);
    }

    @PatchMapping("/ticket/cancel/{uid}")
    public ResponseEntity cancelTicket(@PathVariable String uid) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body("Cancel method not implemented yet");
    }
}
