package ru.study.api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.study.api.dto.PaymentLinkResponse;

@Slf4j
@RequiredArgsConstructor
@Service
public class PaymentLinkServiceImpl implements PaymentLinkService {

    private RequestSequenceService requestSequenceService;


    @Override
    public PaymentLinkResponse paymentLinkByCorrelationId(String correlationId) {
        try {

        } catch (Exception e) {

        }
        requestSequenceService.requestIdByCorrelationUid(correlationId);
        return null;
    }
}
