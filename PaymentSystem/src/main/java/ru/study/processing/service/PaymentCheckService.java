package ru.study.processing.service;

import ru.study.processing.dto.PaymentOrderDto;
import ru.study.processing.dto.PaymentOrderResponse;

public interface PaymentCheckService {

    PaymentOrderResponse pay(PaymentOrderDto paymentOrderDto);
}
