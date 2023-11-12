package ru.study.stub.producer;

import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import ru.nspk.performance.api.TicketRequest;

public interface QueueProducer {

    ListenableFuture<SendResult<Long, byte[]>> send(TicketRequest ticketRequest);
}
