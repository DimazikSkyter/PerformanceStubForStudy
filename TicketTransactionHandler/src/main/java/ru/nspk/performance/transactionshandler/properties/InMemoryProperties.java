package ru.nspk.performance.transactionshandler.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "in-memory")
public class InMemoryProperties {

    private String charset;
    private long timeoutMs;
    private String address;
    private long lockTimeoutMs;
}
