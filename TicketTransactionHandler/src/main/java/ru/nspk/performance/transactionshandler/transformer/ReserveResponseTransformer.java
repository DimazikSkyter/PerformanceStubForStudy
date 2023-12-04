package ru.nspk.performance.transactionshandler.transformer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ru.nspk.performance.action.ReserveResponseAction;
import ru.nspk.performance.transactionshandler.theatreclient.dto.ReserveResponse;

public class ReserveResponseTransformer implements Transformer<String, ReserveResponseAction> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    public ReserveResponseAction transform(String reserveResponse) throws JsonProcessingException {
        ReserveResponse reserve = OBJECT_MAPPER.readValue(reserveResponse, ReserveResponse.class); //контракты
        return ReserveResponseAction.builder()
                .reserveId(reserve.getReserveId())
                .reserveResponseStatus(reserve.getErrorMessage())
                .reserveStarted(reserve.getReserveStarted())
                .reserveDuration(reserve.getReserveDuration())
                .totalAmount(reserve.getTotalAmount())
                .requestId(reserve.getRequestId())
                .build();
    }
}
