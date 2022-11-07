package ru.study.processing.model;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class Ticket {

    private String uidToPay;
    private double price;
    private Instant timeToPayLimit;
    private boolean paid;
    private boolean send;
}
