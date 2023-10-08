package ru.nspk.performance.transactionshandler.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties
@Data
public class TransactionProperties {

    private long paymentLinkTimeoutMs;
    private long fullTimeoutDurationMs;
    private long maxPaymentTimeMs;
    private int timeoutProcessorThreads;
}
