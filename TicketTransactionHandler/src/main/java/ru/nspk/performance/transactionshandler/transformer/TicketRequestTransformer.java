package ru.nspk.performance.transactionshandler.transformer;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import ru.nspk.performance.api.TicketRequest;
import ru.nspk.performance.keyvaluestorage.KeyValueStorage;
import ru.nspk.performance.transactionshandler.model.Seat;
import ru.nspk.performance.transactionshandler.model.theatrecontract.Event;
import ru.nspk.performance.transactionshandler.state.TicketTransactionState;
import ru.nspk.performance.transactionshandler.state.TransactionState;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


@RequiredArgsConstructor
public class TicketRequestTransformer implements Transformer<TicketRequest, TicketTransactionState> {

    public final static String TRANSACTIONS_ID_MAP = "transaction_id";
    DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
    private KeyValueStorage keyValueStorage;

    @Override
    public @NonNull TicketTransactionState transform(TicketRequest in) throws ParseException {
//        keyValueStorage.updateWithCondition()
//                .get("T", )
        return TicketTransactionState.builder()
                //todo разбить на два поля
                .transactionId(in.getRequestId())
                .start(Instant.ofEpochSecond(in.getStart().getSeconds(), in.getStart().getNanos()))
                .event(Event.builder()
                        .eventName(in.getEventName())
                        .eventDate(formatter.parse(in.getEventDate()))
                        .seats(in.getTicketInfoList().stream().map(ticketInfo -> new Seat(ticketInfo.getPlace(), ticketInfo.getPrice(), ticketInfo.getUserUUID())).toList())
                        .build())
                .actions(new HashMap<>())
                .currentState(TransactionState.NEW_TRANSACTION)
                .build();
    }
}
