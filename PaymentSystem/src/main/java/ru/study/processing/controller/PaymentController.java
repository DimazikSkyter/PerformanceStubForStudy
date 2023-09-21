package ru.study.processing.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.study.processing.dto.PaymentDetailsDto;
import ru.study.processing.dto.PaymentOrderDto;
import ru.study.processing.model.PaymentLink;
import ru.study.processing.model.PaymentOrderResponse;
import ru.study.processing.service.PaymentCheckService;
import ru.study.processing.service.PaymentLinkGeneratorService;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/processing/payment")
@Slf4j
@RequiredArgsConstructor
public class PaymentController {

    private static final String INSTANCE_KEY = "INSTANCE_KEY";
    private static final String REQUEST_ID = "REQUEST_ID";
    private final PaymentLinkGeneratorService paymentLinkGeneratorService;
    private final PaymentCheckService paymentCheckService;

    @PostMapping("/paymentLink")
    public PaymentLink paymentLink(@RequestHeader(name = INSTANCE_KEY) String instanceKey, @RequestHeader(name = "REQUEST_ID") String requestId, PaymentDetailsDto paymentDetailsDto) {
        validateInstanceKey(instanceKey);
        return paymentLinkGeneratorService.generate(paymentDetailsDto);
    }

    @PostMapping("/checkPayment")
    public PaymentOrderResponse pay(PaymentOrderDto paymentOrderDto) {
        return paymentCheckService.checkPayment(paymentOrderDto);
    }

    private void validateInstanceKey(String instanceKey) {

    }

    @WebFilter(urlPatterns = {"/processing/payment/*"})
    @Component
    public static class ResponseHeaderFilter implements Filter {

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
            String instanceKey = ((HttpServletRequest) request).getHeader(INSTANCE_KEY);
            if (instanceKey != null) {
                ((HttpServletResponse) response).setHeader(INSTANCE_KEY, instanceKey);
            }

            String requestId = ((HttpServletRequest) request).getHeader(REQUEST_ID);
            if (requestId != null) {
                ((HttpServletResponse) response).setHeader(REQUEST_ID, requestId);
            }
            chain.doFilter(request, response);
        }
    }
}
