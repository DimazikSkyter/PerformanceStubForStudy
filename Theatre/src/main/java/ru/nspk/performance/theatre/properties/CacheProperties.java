package ru.nspk.performance.theatre.properties;


import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Data
@Component
@ConfigurationProperties(prefix = "cache")
@AllArgsConstructor
public class CacheProperties {

    private Duration timeout;
}
