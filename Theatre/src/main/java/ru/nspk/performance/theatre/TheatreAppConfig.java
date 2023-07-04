package ru.nspk.performance.theatre;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.nspk.performance.theatre.model.Event;

import java.util.Map;

@Configuration
public class TheatreAppConfig {

    @Bean
    public Map<String, Event> events() {
        return null;
    }
}
