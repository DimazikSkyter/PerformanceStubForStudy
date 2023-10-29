package ru.nspk.performance.theatre.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
public class Reserve {

    private Event event;
    private Instant createdAt;
    private List<String> seats;
    private double sum;

    public void purchase() {
        event.purchase(seats);
    }
}
