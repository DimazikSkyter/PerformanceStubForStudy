package ru.nspk.performance.transactionshandler.transformer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import ru.nspk.performance.transactionshandler.dto.PaymentOrderResult;

import java.text.ParseException;

public class PaymentOrderResultTransformer implements Transformer<String, PaymentOrderResult> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public @NonNull PaymentOrderResult transform(String in) throws ParseException, JsonProcessingException {
        return OBJECT_MAPPER.readValue(in, PaymentOrderResult.class);
    }
}
