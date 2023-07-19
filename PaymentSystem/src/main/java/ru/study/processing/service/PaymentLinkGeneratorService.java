package ru.study.processing.service;


import ru.study.processing.dto.PaymentDetailsDto;
import ru.study.processing.model.PaymentLink;

public interface PaymentLinkGeneratorService {

    PaymentLink generate(PaymentDetailsDto paymentDetailsDto);
}
