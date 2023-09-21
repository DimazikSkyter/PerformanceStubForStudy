package ru.nspk.performance.transactionshandler.model;

import java.time.Duration;
import java.time.Instant;

public class ReserveResponseEvent extends Event {

    private String reserveResponseStatus;
    private Instant limitToPay;
    private Instant reserveStarted;
    private String reserveDuration = "PT5M";

    public Instant finishTime() {
        Duration timeToPay = Duration.parse(reserveDuration);
        return reserveStarted.plus(timeToPay);
    }
}
