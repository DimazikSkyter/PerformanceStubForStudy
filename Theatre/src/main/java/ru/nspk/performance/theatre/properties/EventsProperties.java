package ru.nspk.performance.theatre.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "events")
@Data
public class EventsProperties {

    private String path;
}
