package ru.nspk.performance.transactionshandler.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties
public class ImdgProperties {

    private String charset;
    private long timeoutMs;
}
