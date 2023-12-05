package ru.nspk.performance.transactionshandler.transformer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import ru.nspk.performance.action.CompleteAction;
import ru.nspk.performance.transactionshandler.model.theatrecontract.PurchaseResponse;

import java.text.ParseException;

public class CompleteTransformer implements Transformer<String, CompleteAction> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    @Override
    public @NonNull CompleteAction transform(String in) throws ParseException, JsonProcessingException {
        PurchaseResponse purchaseResponse = OBJECT_MAPPER.readValue(in, PurchaseResponse.class);
        return CompleteAction.builder()
                .result(purchaseResponse.isResult())
                .requestId(purchaseResponse.getRequestId())
                .build();
    }
}
