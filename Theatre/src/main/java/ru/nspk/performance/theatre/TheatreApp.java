package ru.nspk.performance.theatre;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
public class TheatreApp {

    public static void main(String[] args) {
        SpringApplication.run(TheatreApp.class, args);
    }
}
