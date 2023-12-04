package ru.nspk.performance.transactionshandler.properties;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "payment.client")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentClientProperties {

    private long timeout;
}
