package ru.nspk.performance.transactionshandler.transformer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.NonNull;
import ru.nspk.performance.action.PaymentLinkResponseAction;
import ru.nspk.performance.transactionshandler.dto.PaymentLinkResponse;

import java.text.ParseException;

public class PaymentLinkResponseActionTransformer implements Transformer<String, PaymentLinkResponseAction> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    @Override
    public @NonNull PaymentLinkResponseAction transform(String in) throws ParseException, JsonProcessingException {
        PaymentLinkResponse paymentLinkResponse = OBJECT_MAPPER.readValue(in, PaymentLinkResponse.class);
        return PaymentLinkResponseAction.builder()
                .paymentLinkBytes(paymentLinkResponse.qrBytes())
                .paymentLinkCreated(paymentLinkResponse.created())
                .timeToPayMs(paymentLinkResponse.timeToPayMs())
                .requestId(paymentLinkResponse.requestId())
                .build();
    }
}
