package ru.study.stub.producer;

import org.springframework.util.concurrent.ListenableFuture;
import ru.study.stub.proto.Ticket;

public interface QueueProducer {

    ListenableFuture send(Ticket ticket);
}
