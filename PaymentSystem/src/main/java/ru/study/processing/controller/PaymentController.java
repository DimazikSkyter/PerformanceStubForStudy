package ru.study.processing.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import ru.study.processing.client.TicketTransactionClient;
import ru.study.processing.dto.PaymentDetailsDto;
import ru.study.processing.dto.PaymentOrderDto;
import ru.study.processing.dto.PaymentLinkResponse;
import ru.study.processing.dto.PaymentOrderResponse;
import ru.study.processing.service.PaymentCheckService;
import ru.study.processing.service.PaymentLinkGeneratorService;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/processing/payment")
@Slf4j
@RequiredArgsConstructor
public class PaymentController {

    private static final String REQUEST_ID = "REQUEST_ID";
    private final PaymentLinkGeneratorService paymentLinkGeneratorService;
    private final PaymentCheckService paymentCheckService;
    private final TicketTransactionClient ticketTransactionClient;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @PostMapping("/payment_link")
    public PaymentLinkResponse paymentLink(@RequestHeader(name = "REQUEST_ID") String requestId, @RequestBody PaymentDetailsDto paymentDetailsDto) {
        log.info("New request {} for dto {}", requestId, paymentDetailsDto);
        return paymentLinkGeneratorService.generate(requestId, paymentDetailsDto);
    }

    @PostMapping("/pay")
    public PaymentOrderResponse pay(PaymentOrderDto paymentOrderDto) {
        PaymentOrderResponse paymentOrderResponse = paymentCheckService.pay(paymentOrderDto);
        //todo временный костыль
        paymentOrderResponse.setRequestId(paymentOrderDto.getSecretPaymentIdentification());
        executorService.execute(() -> ticketTransactionClient.sendStatusToTicketTransactionService(paymentOrderResponse));
        return paymentOrderResponse;
    }

    @WebFilter(urlPatterns = {"/processing/payment/*"})
    @Component
    public static class ResponseHeaderFilter implements Filter {

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
            String requestId = ((HttpServletRequest) request).getHeader(REQUEST_ID);
            if (requestId != null) {
                ((HttpServletResponse) response).setHeader(REQUEST_ID, requestId);
            }
            chain.doFilter(request, response);
        }
    }
}
