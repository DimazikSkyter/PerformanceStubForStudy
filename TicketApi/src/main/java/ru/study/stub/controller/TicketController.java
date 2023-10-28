package ru.study.stub.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.study.stub.dto.Subscriber;
import ru.study.stub.dto.TicketDto;
import ru.study.stub.exception.EventNotFoundException;
import ru.study.stub.service.TicketService;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/storage/api")
@AllArgsConstructor
public class TicketController implements TicketApi {

    private TicketService ticketService;

    @PutMapping("/ticket")
    public ResponseEntity buyTickets(@RequestBody TicketDto ticketDto) {
        try {
            return ResponseEntity.ok(ticketService.createNewTicket(ticketDto));
        } catch (EventNotFoundException ex) {
            return ResponseEntity.status(404).body(Map.of("error",
                    String.format("Event %s not found.", ex.getEventName())));
        } catch (Exception ex) {
            log.error("Failed to create ticket. Catch exception", ex);
            return ResponseEntity.internalServerError().body(Map.of(
                    "error",
                    String.format("Failed to create ticket for person %s and event %s",
                            ticketDto.getPerson(),
                            ticketDto.getEvent())));
        }
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
        return null;
    }
}
