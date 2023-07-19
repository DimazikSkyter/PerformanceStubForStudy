package ru.nspk.performance.theatre.properties;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Data
@Configuration
@ConfigurationProperties(prefix = "cache")
@AllArgsConstructor
@NoArgsConstructor
public class CacheProperties {

    private Duration timeout;
}
