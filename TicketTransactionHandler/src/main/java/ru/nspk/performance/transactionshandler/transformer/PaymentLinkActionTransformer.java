package ru.nspk.performance.transactionshandler.transformer;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.NonNull;
import org.apache.kafka.common.Uuid;
import ru.nspk.performance.action.PaymentLinkAction;
import ru.nspk.performance.action.ReserveResponseAction;

import java.text.ParseException;

public class PaymentLinkActionTransformer implements Transformer<ReserveResponseAction, PaymentLinkAction> {

    @Override
    public @NonNull PaymentLinkAction transform(ReserveResponseAction in) throws ParseException, JsonProcessingException {
        return PaymentLinkAction.builder()
                .transactionId(in.getTransactionId())
                .amount(in.getTotalAmount())
                .requestId(Uuid.randomUuid().toString())
                .build();
    }
}
