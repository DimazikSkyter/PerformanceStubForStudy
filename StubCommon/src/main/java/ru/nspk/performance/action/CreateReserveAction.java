package ru.nspk.performance.action;

import lombok.*;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Data
@ToString(callSuper = true)
@SuperBuilder
@Jacksonized
public class CreateReserveAction extends Action {

    private final String seats;

    public CreateReserveAction(ActionBuilder<?, ?> b, String seats, Long transactionId) {
        super(b);
        super.setTransactionId(transactionId);
        this.seats = seats;
    }
}
