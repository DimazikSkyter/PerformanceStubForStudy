package ru.study.api.properties;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "theatre")
@Component
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class TheatreProperty  {

    private String baseUrl;
    private long clientTimeoutMs;
    private int retries;
}
