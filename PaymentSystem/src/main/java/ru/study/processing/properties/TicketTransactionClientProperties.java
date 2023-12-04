package ru.study.processing.properties;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;


@Data
@Component
@ConfigurationProperties(prefix = "ticket.client")
public class TicketTransactionClientProperties {

    private String fullUrl;
    private Duration readTimeout;
    private Duration connectTimeout;
}
