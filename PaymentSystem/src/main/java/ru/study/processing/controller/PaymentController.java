package ru.study.processing.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.study.processing.exception.AlreadyPaidException;
import ru.study.processing.exception.TicketNotFound;
import ru.study.processing.exception.WrongSumException;
import ru.study.processing.service.Processing;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/processing/payment")
@Slf4j
@RequiredArgsConstructor
public class PaymentController {

    private final Processing processing;

    @PostMapping()
    public ResponseEntity payment(@RequestParam String uid, @RequestParam double price) {
        try {
            processing.payForTicket(uid, price);
            return ResponseEntity.ok(Map.of("uid", uid,
                    "message", "Ticket successfully paid. Ticket handler successfully inform."));
        } catch (TicketNotFound ex) {
            log.error("Failed to find ticket with uid {}", uid);
            return ResponseEntity.notFound().build();
        } catch (WrongSumException ex) {
            log.error("Wrong price {} of ticket with uid {}.", price, uid);
            return  ResponseEntity.status(400).body("Wrong price.");
        } catch (AlreadyPaidException ex) {
            log.error("Ticket {} already paid.", uid);
            return ResponseEntity.status(400).body("Ticket already paid.");
        } catch (Exception ex) {
            return ResponseEntity.status(504).body(Map.of("uid", uid,
                    "message", "Ticket successfully paid. Failed to send inform to ticket handler."));
        }
    }
}
