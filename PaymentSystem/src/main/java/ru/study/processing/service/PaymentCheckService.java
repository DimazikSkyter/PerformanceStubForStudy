package ru.study.processing.service;

import ru.study.processing.dto.PaymentOrderDto;
import ru.study.processing.model.PaymentOrderResponse;

public interface PaymentCheckService {

    PaymentOrderResponse checkPayment(PaymentOrderDto paymentOrderDto);
}
