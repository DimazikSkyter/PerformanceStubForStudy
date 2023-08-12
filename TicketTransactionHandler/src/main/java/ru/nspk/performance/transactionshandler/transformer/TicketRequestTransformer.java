package ru.nspk.performance.transactionshandler.transformer;

import ru.nspk.performance.api.TicketRequest;
import ru.nspk.performance.transactionshandler.state.TicketTransaction;

public interface TicketRequestTransformer {

    TicketTransaction transform(TicketRequest ticketRequest);
}
