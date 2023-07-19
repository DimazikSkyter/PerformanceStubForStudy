package ru.study.processing.service;

import ru.study.processing.dto.PaymentOrderDto;
import ru.study.processing.model.PaymentOrderResponse;

public class PaymentCheckServiceStub implements PaymentCheckService {

    @Override
    public PaymentOrderResponse checkPayment(PaymentOrderDto paymentOrderDto) {
        //String secretKey, String secretPaymentIdentification
        return PaymentOrderResponse.builder()
                .status("Success")
                .build();
    }
}
