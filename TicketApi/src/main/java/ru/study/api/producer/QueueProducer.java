package ru.study.api.producer;

import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import ru.nspk.performance.api.TicketRequest;

import java.util.concurrent.CompletableFuture;

public interface QueueProducer {

    CompletableFuture<SendResult<Long, byte[]>> send(TicketRequest ticketRequest);
}
