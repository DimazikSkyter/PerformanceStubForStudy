package ru.nspk.performance.transactionshandler.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.nspk.performance.transactionshandler.dto.PaymentOrderResult;
import ru.nspk.performance.transactionshandler.service.TransactionalEventService;

@Controller
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentServiceController {

    private final TransactionalEventService transactionalEventService;

    @PostMapping("/update_status")
    @ResponseStatus(value = HttpStatus.OK)
    public void updateTransactionStatus(@RequestBody String paymentResult) {
        transactionalEventService.handlePaymentResult(paymentResult);
    }
}
