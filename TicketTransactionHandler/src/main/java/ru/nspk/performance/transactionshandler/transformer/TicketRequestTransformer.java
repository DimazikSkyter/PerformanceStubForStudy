package ru.nspk.performance.transactionshandler.transformer;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestHeader;
import ru.nspk.performance.api.TicketRequest;
import ru.nspk.performance.transactionshandler.keyvaluestorage.KeyValueStorage;
import ru.nspk.performance.transactionshandler.state.TicketTransactionState;
import ru.nspk.performance.transactionshandler.state.TransactionState;


@RequiredArgsConstructor
public class TicketRequestTransformer implements Transformer<TicketRequest, TicketTransactionState> {

    public final static String TRANSACTIONS_ID_MAP = "transaction_id";
    private KeyValueStorage keyValueStorage;

    @Override
    public @NonNull TicketTransactionState transform(TicketRequest in) {
//        keyValueStorage.updateWithCondition()
//                .get("T", )
        return TicketTransactionState.builder().build();
    }
}
