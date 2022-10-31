package ru.study.stub.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "application.kafka")
public class KafkaProperty {

    private int partitions;
    private int replicas;
    private String name;
}
