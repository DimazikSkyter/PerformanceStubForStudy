package ru.nspk.performance.transactionshandler.transformer;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.NonNull;
import org.apache.kafka.common.Uuid;
import ru.nspk.performance.action.NotifyTheatreAction;
import ru.nspk.performance.transactionshandler.state.TicketTransactionState;

import java.text.ParseException;

public class NotifyTheatreActionTransformer implements Transformer<TicketTransactionState, NotifyTheatreAction> {

    @Override
    public @NonNull NotifyTheatreAction transform(TicketTransactionState in) throws ParseException, JsonProcessingException {
        return NotifyTheatreAction.builder()
                .reserveId(in.getEvent().getReserveId())
                .requestId(Uuid.randomUuid().toString())
                .transactionId(in.getTransactionId())
                .build();
    }
}
