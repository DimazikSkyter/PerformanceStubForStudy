package ru.nspk.performance.transactionshandler.properties;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Component
@ConfigurationProperties(prefix = "theatre.client")
public class TheatreClientProperties {
    private long timeout;
    private long purchaseTimeout;
}
