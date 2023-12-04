package ru.nspk.performance.transactionshandler.transformer;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.NonNull;
import ru.nspk.performance.action.PaymentResultAction;
import ru.nspk.performance.transactionshandler.dto.PaymentOrderResult;

import java.text.ParseException;

public class PaymentResultActionTransformer implements Transformer<PaymentOrderResult, PaymentResultAction> {

    @Override
    public @NonNull PaymentResultAction transform(PaymentOrderResult in) throws ParseException, JsonProcessingException {
        return PaymentResultAction.builder()
                .errorMessage(in.isSuccess() ? null : in.getStatus())
                .paymentSuccess(in.isSuccess())
                .build();
    }
}
