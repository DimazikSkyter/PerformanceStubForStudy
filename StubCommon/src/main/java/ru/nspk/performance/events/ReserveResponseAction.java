package ru.nspk.performance.events;

import lombok.Data;

import java.time.Duration;
import java.time.Instant;

@Data
public class ReserveResponseAction extends Action {

    private String requestId;
    private Long reserveId;
    private String reserveResponseStatus;
    private Instant limitToPay;
    private Instant reserveStarted;
    private String reserveDuration = "PT5M";

    public Instant finishTime() {
        Duration timeToPay = Duration.parse(reserveDuration);
        return reserveStarted.plus(timeToPay);
    }
}
