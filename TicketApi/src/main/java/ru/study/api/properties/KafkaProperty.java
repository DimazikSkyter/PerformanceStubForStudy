package ru.study.api.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "kafka")
public class KafkaProperty {

    private int partitions;
    private int replicas;
    private String name;
}
