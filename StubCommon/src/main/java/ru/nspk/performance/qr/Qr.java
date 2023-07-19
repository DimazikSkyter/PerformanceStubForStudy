package ru.nspk.performance.qr;

import lombok.Data;

import java.time.Instant;

@Data
public class Qr {

    private String uid;
    private String target;
    private Instant timestamp;
    private double amount;
}
