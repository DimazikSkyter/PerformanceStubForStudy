package ru.study.processing.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.study.processing.dto.PaymentDetailsDto;
import ru.study.processing.dto.PaymentOrderDto;
import ru.study.processing.exception.AlreadyPaidException;
import ru.study.processing.exception.TicketNotFound;
import ru.study.processing.exception.WrongSumException;
import ru.study.processing.model.PaymentLink;
import ru.study.processing.model.PaymentOrderResponse;
import ru.study.processing.service.PaymentCheckService;
import ru.study.processing.service.PaymentLinkGeneratorService;

import java.util.Map;

@RestController
@RequestMapping("/processing/payment")
@Slf4j
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentLinkGeneratorService paymentLinkGeneratorService;
    private final PaymentCheckService paymentCheckService;


    @PostMapping("/paymentLink")
    public PaymentLink paymentLink(@RequestHeader String instanceKey,  PaymentDetailsDto paymentDetailsDto) {
        validateInstanceKey(instanceKey);
        return paymentLinkGeneratorService.generate(paymentDetailsDto);
    }

    @PostMapping("/checkPayment")
    public PaymentOrderResponse pay(PaymentOrderDto paymentOrderDto) {
        return paymentCheckService.checkPayment(paymentOrderDto);
    }

    private void validateInstanceKey(String instanceKey) {

    }
}
