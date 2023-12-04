package ru.nspk.performance.transactionshandler.properties;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "kafka")
@Component
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KafkaProperties {

    //not used
    private String bootstrapAddress;
    private String actionTopic;
    private String completeActionTopic;
    private String transactionStateTopic;
    private String paymentLinkTopic;
}
