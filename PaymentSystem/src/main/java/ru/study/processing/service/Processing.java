package ru.study.processing.service;


import ru.study.processing.model.Ticket;

public interface Processing {

    void addNewTicket(Ticket ticket);

    void payForTicket(String uid, double sum);
}
