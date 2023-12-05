package ru.study.api.service;

import ru.study.api.dto.PaymentLinkResponse;

public interface PaymentLinkService {

    PaymentLinkResponse paymentLinkByCorrelationId(String correlationId);
}
