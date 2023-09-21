package ru.nspk.performance.transactionshandler.transformer;

import ru.nspk.performance.theatre.model.ReserveResponse;
import ru.nspk.performance.transactionshandler.model.ReserveResponseEvent;

public class ReserveResponseTransformer implements Transformer<ReserveResponse, ReserveResponseEvent> {

    public ReserveResponseEvent transform(ReserveResponse reserveResponse) {
        return null;
    }
}
