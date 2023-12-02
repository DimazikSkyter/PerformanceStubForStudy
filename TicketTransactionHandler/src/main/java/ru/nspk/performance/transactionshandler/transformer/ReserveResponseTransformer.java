package ru.nspk.performance.transactionshandler.transformer;

import ru.nspk.performance.theatre.dto.ReserveResponse;
import ru.nspk.performance.events.ReserveResponseAction;

import java.time.Instant;

public class ReserveResponseTransformer implements Transformer<String, ReserveResponseAction> {

    public ReserveResponseAction transform(String reserveResponse) {

        return ReserveResponseAction.builder()
                .reserveId(reserveResponse.getReserveId())
                .reserveResponseStatus(reserveResponse.getErrorMessage())
                .reserveStarted(reserveResponse.getReserveStarted())
                .reserveDuration(reserveResponse.getReserveDuration())
                .build();
    }
}
