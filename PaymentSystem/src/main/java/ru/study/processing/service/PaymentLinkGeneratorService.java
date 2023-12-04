package ru.study.processing.service;


import ru.study.processing.dto.PaymentDetailsDto;
import ru.study.processing.dto.PaymentLinkResponse;

public interface PaymentLinkGeneratorService {

    PaymentLinkResponse generate(String requestId, PaymentDetailsDto paymentDetailsDto);
}
