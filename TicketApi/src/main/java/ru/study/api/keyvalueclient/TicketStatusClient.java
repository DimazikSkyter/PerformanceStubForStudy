package ru.study.api.keyvalueclient;

import ru.study.api.model.TicketStatus;

public interface TicketStatusClient {

    TicketStatus checkTicketStatus(long requestId);
}
