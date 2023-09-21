package ru.nspk.performance.transactionshandler;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.nspk.performance.api.TicketRequest;
import ru.nspk.performance.transactionshandler.model.PaymentCheckResponse;
import ru.nspk.performance.transactionshandler.model.ReserveResponseEvent;
import ru.nspk.performance.transactionshandler.transformer.ReserveResponseTransformer;
import ru.nspk.performance.transactionshandler.transformer.TicketRequestTransformer;
import ru.nspk.performance.transactionshandler.transformer.Transformer;
import ru.nspk.performance.transactionshandler.transformer.TransformerMultiton;
import ru.nspk.performance.transactionshandler.validator.*;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class AppConfiguration {

    @Bean
    public TransformerMultiton transformerMultiton() {
        Map<Class, Transformer> transformers = new HashMap<>();
        transformers.put(ReserveResponseTransformer.class, new ReserveResponseTransformer());
        transformers.put(TicketRequest.class, new TicketRequestTransformer());
        return new TransformerMultiton(transformers);
    }

    @Bean
    public ValidatorMultiton validatorMultiton() {
        Map<Class, Validator> validators = new HashMap<>();
        validators.put(PaymentCheckResponse.class, new PaymentCheckResponseValidator());
        validators.put(ReserveResponseEvent.class, new ReserveResponseEventValidator());
        validators.put(TicketRequest.class, new TicketRequestValidator());
        return new ValidatorMultiton(validators);
    }
}
