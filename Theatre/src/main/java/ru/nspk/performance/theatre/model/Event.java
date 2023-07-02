package ru.nspk.performance.theatre.model;

import lombok.Data;

import java.util.Map;

@Data
public class Event {

    private String name;
    private Map<String, SeatStatus> seats;
}
