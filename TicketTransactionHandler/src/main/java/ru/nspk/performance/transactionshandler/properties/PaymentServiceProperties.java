package ru.nspk.performance.transactionshandler.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Data
@Component
@ConfigurationProperties(prefix = "payment-service")
public class PaymentServiceProperties {

    private String paymentServiceBaseAddress;
    private String paymentServicePath;
    private String instanceKey;
}
