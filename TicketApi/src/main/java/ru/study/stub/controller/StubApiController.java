package ru.study.stub.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.study.stub.dto.TicketDto;
import ru.study.stub.exception.EventNotFoundException;
import ru.study.stub.service.TicketService;

import java.util.Map;

@RestController
@RequestMapping("/stub")
@AllArgsConstructor
public class StubApiController {

    private TicketService ticketService;

    @PutMapping("/api/ticket")
    public ResponseEntity createNewTicket(TicketDto ticketDto) {
        try {
            return ResponseEntity.ok(ticketService.createNewTicket(ticketDto));
        } catch (EventNotFoundException ex) {
            return ResponseEntity.status(404).body(Map.of("error",
                    String.format("Event %s not found.", ex.getEventName())));
        }  catch (Exception ex) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error",
                    String.format("Failed to create ticket for person %s and event %s",
                            ticketDto.getPerson(),
                            ticketDto.getEvent())));
        }
    }
}
