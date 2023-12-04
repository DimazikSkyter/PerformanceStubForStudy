package ru.study.processing.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import ru.study.processing.dto.PaymentOrderResponse;

public interface TicketTransactionClient {

    void sendStatusToTicketTransactionService(PaymentOrderResponse paymentOrderResponse);
}
