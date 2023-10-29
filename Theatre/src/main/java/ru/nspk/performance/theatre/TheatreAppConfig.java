package ru.nspk.performance.theatre;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import ru.nspk.performance.theatre.model.Event;
import ru.nspk.performance.theatre.properties.EventsProperties;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Configuration
public class TheatreAppConfig {



    @Bean
    public Map<String, Event> events(EventsProperties eventsProperties) throws IOException {
        String eventsFile = new String(Objects.requireNonNull(Files.readAllBytes(Path.of(eventsProperties.getPath()))));
        TypeReference<Map<String, Event>> typeReference = new TypeReference<Map<String, Event>>() {
        };
        log.debug("Events file is: \n{}", eventsFile);
        return new ObjectMapper().registerModule( new ParameterNamesModule() ).readValue(eventsFile, typeReference);
    }
}
