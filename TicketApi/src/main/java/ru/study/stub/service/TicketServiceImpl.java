package ru.study.stub.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import ru.study.stub.dto.TicketDto;
import ru.study.stub.entity.TicketStatus;
import ru.study.stub.model.TicketResponse;
import ru.study.stub.producer.QueueProducer;
import ru.study.stub.proto.Ticket;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@AllArgsConstructor
@Slf4j
public class TicketServiceImpl implements TicketService {

    private final EventService eventService;
    private final QueueProducer queueProducer;

    @Override
    public TicketResponse createNewTicket(TicketDto ticketDto) {
        double eventAndGetItPrice = eventService.findEventAndGetItPrice(ticketDto.getEvent());

        Instant start = Instant.now();
        Ticket ticket = transform(ticketDto, eventAndGetItPrice, start);

        save(ticket);

        return TicketResponse.builder()
                .creation(start)
                .fio(ticketDto.getPerson().getFio())
                .address(ticketDto.getPerson().getAddress())
                .age(ticketDto.getPerson().getAge())
                .event(ticket.getEventName())
                .price(eventAndGetItPrice)
                .timeToPay(eventService.getTimeToPay(ticketDto.getEvent()))
                .uidToPay(ticket.getUidToPay())
                .eventLevel(ticketDto.getEvent().getEventLevel())
                .build();
    }

    @Override
    public TicketStatus checkTicket(String uid) {
        return null;
    }

    private boolean save(Ticket ticket) {
        var future = queueProducer
                .send(ticket);

        AtomicBoolean atomicBoolean = addListener(ticket, future);

        await(ticket);

        if (atomicBoolean.get()) {
            throw new RuntimeException("Catch exception while save a ticket.");
        }

        return true;
    }

    private void await(Ticket ticket) {
        synchronized (ticket) {
            try {
                //todo add timeout notifier
                ticket.wait();
            } catch (InterruptedException e) {
                log.error("Catch error while wait");
            }
        }
    }

    private AtomicBoolean addListener(Ticket ticket, ListenableFuture future) {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        future.addCallback(new ListenableFutureCallback<SendResult<String, Ticket>>() {
            @Override
            public void onSuccess(SendResult<String, Ticket> result) {
                log.info("Ticket {} successfully saved", ticket.getUidToPay());
                synchronized (ticket) {
                    atomicBoolean.set(true);
                    ticket.notifyAll();
                }
            }

            @Override
            public void onFailure(Throwable ex) {
                log.error("Failed to saved ticket {}", ticket.getUidToPay(), ex);
                synchronized (ticket) {
                    notifyAll();
                }
            }
        });
        return atomicBoolean;
    }

    private Ticket transform(TicketDto ticketDto, double price, Instant start) {
        return Ticket.newBuilder()
                .setAddress(ticketDto.getPerson().getAddress())
                .setPrice(price)
                .setAge(ticketDto.getPerson().getAge())
                .setCreation(start.getEpochSecond())
                .setEventName(ticketDto.getEvent().getEventName())
                .setFio(ticketDto.getPerson().getFio())
                .setEventType(ticketDto.getEvent().getEventLevel().getEventType())
                .setUidToPay(UUID.randomUUID().toString())
                .build();
    }
}
