package ru.nspk.performance.action;

import lombok.Data;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import java.time.Duration;
import java.time.Instant;

@Data
@SuperBuilder
@Jacksonized
public class ReserveResponseAction extends Action {

    private String requestId;
    private Long reserveId;
    private String reserveResponseStatus;
    private Instant limitToPay;
    private Instant reserveStarted;
    private String reserveDuration = "PT5M";
    private double totalAmount;

    public ReserveResponseAction(ActionBuilder<?, ?> b, Long reserveId) {
        super(b);
        this.reserveId = reserveId;
    }

    public Instant finishTime() {
        Duration timeToPay = Duration.parse(reserveDuration);
        return reserveStarted.plus(timeToPay);
    }
}
