package ru.nspk.performance.transactionshandler.transformer;

import lombok.NonNull;
import ru.nspk.performance.api.TicketRequest;
import ru.nspk.performance.transactionshandler.state.TransactionState;

public class TicketRequestTransformer implements Transformer<TicketRequest, TransactionState> {

    @Override
    public @NonNull TransactionState transform(TicketRequest in) {
        return null;
    }
}
