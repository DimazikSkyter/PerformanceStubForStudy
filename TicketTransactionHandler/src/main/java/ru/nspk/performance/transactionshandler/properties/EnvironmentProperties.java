package ru.nspk.performance.transactionshandler.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "environment")
public class EnvironmentProperties {
    private String theatreBaseUrl;
    private String paymentServiceBaseUrl;
}
