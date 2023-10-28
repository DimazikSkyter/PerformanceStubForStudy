package ru.nspk.performance.transactionshandler.transformer;

import ru.nspk.performance.theatre.dto.ReserveResponse;
import ru.nspk.performance.events.ReserveResponseAction;

public class ReserveResponseTransformer implements Transformer<ReserveResponse, ReserveResponseAction> {

    public ReserveResponseAction transform(ReserveResponse reserveResponse) {
        return null;
    }
}
