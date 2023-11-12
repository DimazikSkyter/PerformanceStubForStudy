package ru.study.stub.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.study.stub.dto.TicketDto;
import ru.study.stub.dto.TicketPurchaseRequest;
import ru.study.stub.dto.TicketPurchaseResponse;
import ru.study.stub.service.TicketService;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/storage/api")
@AllArgsConstructor
public class TicketController {

    private TicketService ticketService;

    @PutMapping("/ticket/purchase")
    public TicketPurchaseResponse buyTickets(@RequestBody TicketPurchaseRequest request) {
        return ticketService.createNewTicket(request);
    }

    @GetMapping("/ticket/status/{uid}")
    public ResponseEntity checkTicketStatus(@PathVariable("uid") String uid) {
        try {
            return ResponseEntity.ok(ticketService.checkTicket(uid));
        } catch (NullPointerException ex) {
            return ResponseEntity.status(404).body(
                    Map.of("error", "Ticket " + uid + " not found."));
        } catch (Exception ex) {
            log.error("Failed to create ticket. Catch exception", ex);
            return ResponseEntity.internalServerError().body(
                    Map.of("error", "Sorry, failed to get status of ticket " + uid));
        }
    }

    @PatchMapping("/ticket/cancel/{uid}")
    public ResponseEntity cancelTicket(@PathVariable String uid) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body("Cancel method not implemented yet");
    }
}
