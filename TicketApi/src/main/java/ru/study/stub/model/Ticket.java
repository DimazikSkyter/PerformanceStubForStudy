package ru.study.stub.model;

import lombok.Data;

import java.time.Duration;
import java.time.Instant;

@Data
public class Ticket {

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
