package ru.study.api.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "in-memory")
public class InMemoryProperties {

    private String charset;
    private long timeToLive;
    private long timeout;
    private String address;
    private long lockTimeoutMs;
}
