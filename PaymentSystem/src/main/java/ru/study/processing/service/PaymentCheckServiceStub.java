package ru.study.processing.service;

import org.springframework.stereotype.Service;
import ru.study.processing.dto.PaymentOrderDto;
import ru.study.processing.dto.PaymentOrderResponse;

@Service
public class PaymentCheckServiceStub implements PaymentCheckService {

    @Override
    public PaymentOrderResponse pay(PaymentOrderDto paymentOrderDto) {
        //String secretKey, String secretPaymentIdentification
        return PaymentOrderResponse.builder()
                .status("Success")
                .build();
    }
}
