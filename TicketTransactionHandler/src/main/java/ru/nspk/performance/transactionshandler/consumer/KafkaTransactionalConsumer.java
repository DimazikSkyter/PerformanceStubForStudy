package ru.nspk.performance.transactionshandler.consumer;


import com.google.protobuf.InvalidProtocolBufferException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.security.oauthbearer.internals.secured.Retryable;
import org.springframework.kafka.annotation.KafkaListener;
import ru.nspk.performance.api.TicketRequest;
import ru.nspk.performance.transactionshandler.service.TransactionalEventService;
import ru.study.stub.proto.Ticket;

@Slf4j
@RequiredArgsConstructor
public class KafkaTransactionalConsumer {

    private final TransactionalEventService transactionalEventService;

    @KafkaListener(topics = "ticket_request", groupId = "reader")
    public void newTransaction(ConsumerRecord<Long, byte[]> record) throws InvalidProtocolBufferException {
        try {
            TicketRequest ticketRequest = TicketRequest.parseFrom(record.value());
            log.info("New Income ticket request {}", ticketRequest);
            transactionalEventService.newTicketEvent(ticketRequest);
        } catch (Exception e) {
            if (e instanceof Retryable) {
                log.debug("Throwing retryable exception.");
                throw e;
            }
            log.error("Error processing message: " + e.getMessage());
        }
    }
}
