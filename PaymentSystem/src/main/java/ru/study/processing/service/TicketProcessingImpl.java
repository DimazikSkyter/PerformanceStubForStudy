package ru.study.processing.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.web.client.RestTemplate;
import ru.study.processing.exception.AlreadyPaidException;
import ru.study.processing.exception.TicketNotFound;
import ru.study.processing.exception.WrongSumException;
import ru.study.processing.model.Ticket;

import java.util.Map;

@Slf4j
public class TicketProcessingImpl implements Processing {

    //todo переделать на нормальный кэш, синхронизация
    private Cache cache = new ConcurrentMapCache("tickets", false);
    RestTemplate restTemplate = new RestTemplate();
    String fooResourceUrl
            = "http://localhost:8080/spring-rest/ticket/";

    @Override
    public void addNewTicket(Ticket ticket) {
        Object o = cache.putIfAbsent(ticket.getUidToPay(), ticket).get();
        if (o != null) {
            log.warn("Ticket {} already exist in cache. Nothing to do.", ticket);
        }
    }

    @Override
    public void payForTicket(String uid, double sum) {
        Ticket ticket = cache.get(uid, Ticket.class);
        if (ticket == null) {
            throw new TicketNotFound(uid);
        } else if (ticket.getPrice() != sum) {
            throw new WrongSumException(uid, sum);
        }

        if (ticket.isPaid()) {
            throw new AlreadyPaidException(uid);
        }
        ticket.setPaid(true);

        String response = restTemplate.postForObject(fooResourceUrl, null, String.class, Map.of("uid", uid));
        if ("ok".equals(response)) {
            ticket.setSend(true);
        }
    }
}
