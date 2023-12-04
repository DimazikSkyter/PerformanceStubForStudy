package ru.study.processing.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.study.processing.dto.PaymentOrderResponse;
import ru.study.processing.properties.TicketTransactionClientProperties;

import java.time.Duration;

@Component
@Slf4j
public class TicketTransactionClientImpl implements TicketTransactionClient {

    private final RestTemplate restTemplate;
    private final TicketTransactionClientProperties properties;

    public TicketTransactionClientImpl(RestTemplateBuilder restTemplateBuilder, TicketTransactionClientProperties properties) {
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(properties.getConnectTimeout())
                .setReadTimeout(properties.getReadTimeout())
                .build();
        this.properties = properties;
    }

    @Override
    public void sendStatusToTicketTransactionService(PaymentOrderResponse paymentOrderResponse) {
        try {
            HttpEntity<PaymentOrderResponse> request =
                    new HttpEntity<>(paymentOrderResponse);
            restTemplate.postForLocation(properties.getFullUrl(), request);
        } catch (Exception e) {
            log.error("Failed to send payment order response {}", paymentOrderResponse, e);
        }
    }
}
