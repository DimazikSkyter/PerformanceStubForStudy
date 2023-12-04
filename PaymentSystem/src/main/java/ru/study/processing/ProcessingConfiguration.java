package ru.study.processing;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.nspk.performance.qr.QrStreamDecorator;

@Configuration
public class ProcessingConfiguration {

    @Bean
    public QrStreamDecorator qrStreamDecorator() {
        return new QrStreamDecorator(120);
    }
}
