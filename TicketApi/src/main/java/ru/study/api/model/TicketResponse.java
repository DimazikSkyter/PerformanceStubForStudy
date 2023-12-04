package ru.study.api.model;

import lombok.Builder;
import lombok.Data;

import java.time.Duration;
import java.time.Instant;

@Builder
@Data
public class TicketResponse {

    private String fio;
    private int age;
    private String address;
    private double price;
    private Instant creation;
    private Duration timeToPay;
    private String uidToPay;
    private String event;
    private EventLevel eventLevel;
}
