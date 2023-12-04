package ru.study.api.keyvalueclient;

import org.springframework.stereotype.Component;
import ru.study.api.model.TicketStatus;

@Component
public class HazelcastTicketStatusClient implements TicketStatusClient {

    @Override
    public TicketStatus checkTicketStatus(long requestId) {
        return new TicketStatus(requestId, "Not implemented");
    }
}
