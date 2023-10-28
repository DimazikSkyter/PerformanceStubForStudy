package ru.performance.transactionhandler.consumer;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.security.oauthbearer.secured.Retryable;
import org.springframework.kafka.annotation.KafkaListener;
import ru.nspk.performance.api.TicketRequest;
import ru.performance.transactionhandler.model.Event;
import ru.performance.transactionhandler.service.TransactionalEventService;


@Slf4j
@RequiredArgsConstructor
public class KafkaTransactionalConsumer {

    private final TransactionalEventService transactionalEventService;

    @KafkaListener(topics = "new_transaction_requests", groupId = "reader")
    public void newTransaction(ConsumerRecord<?, TicketRequest> record) {
        try {
            transactionalEventService.newTicketEvent(record.value());
        } catch (Exception e) {
            if (e instanceof Retryable) {
                log.debug("Throwing retryable exception.");
                throw e;
            }
            log.error("Error processing message: " + e.getMessage());
        }
    }

    @KafkaListener(topics = "events", groupId = "reader")
    public void printEvent(ConsumerRecord<?, Event> record) {
        log.info("Income event {}", record.value());
    }
}