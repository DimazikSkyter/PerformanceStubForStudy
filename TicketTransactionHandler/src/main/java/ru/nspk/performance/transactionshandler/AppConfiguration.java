package ru.nspk.performance.transactionshandler;


import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import org.eclipse.jetty.client.HttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.nspk.performance.api.TicketRequest;
import ru.nspk.performance.transactionshandler.keyvaluestorage.HazelcastKeyValue;
import ru.nspk.performance.transactionshandler.model.PaymentCheckResponse;
import ru.nspk.performance.transactionshandler.model.ReserveResponseEvent;
import ru.nspk.performance.transactionshandler.properties.EnvironmentProperties;
import ru.nspk.performance.transactionshandler.properties.InMemoryProperties;
import ru.nspk.performance.transactionshandler.properties.TransactionProperties;
import ru.nspk.performance.transactionshandler.service.TransactionalEventService;
import ru.nspk.performance.transactionshandler.state.TicketTransactionState;
import ru.nspk.performance.transactionshandler.theatreclient.TheatreClient;
import ru.nspk.performance.transactionshandler.theatreclient.TheatreClientImpl;
import ru.nspk.performance.transactionshandler.timeoutprocessor.TimeoutProcessor;
import ru.nspk.performance.transactionshandler.transformer.ReserveResponseTransformer;
import ru.nspk.performance.transactionshandler.transformer.TicketRequestTransformer;
import ru.nspk.performance.transactionshandler.transformer.Transformer;
import ru.nspk.performance.transactionshandler.transformer.TransformerMultiton;
import ru.nspk.performance.transactionshandler.validator.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

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

    @Bean
    public ClientConfig clientConfig(InMemoryProperties inMemoryProperties) {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.getNetworkConfig().addAddress(inMemoryProperties.getAddress());
        return clientConfig;
    }

    @Bean
    public Map<String, HazelcastKeyValue> keyValueMaps(HazelcastInstance hazelcastInstance,
                                                       InMemoryProperties inMemoryProperties) {
        Map<String, HazelcastKeyValue> maps = new HashMap<>();
        //requests
        IMap<String, Long> requestsMap = hazelcastInstance.getMap(TransactionalEventService.REQUESTS_MAP);
        maps.put(TransactionalEventService.REQUESTS_MAP, new HazelcastKeyValue(requestsMap, inMemoryProperties));

        IMap<Long, TicketTransactionState> transactionStateMap = hazelcastInstance.getMap(TransactionalEventService.TRANSACTION_MAP);
        maps.put(TransactionalEventService.TRANSACTION_MAP, new HazelcastKeyValue(transactionStateMap, inMemoryProperties));
        return maps;
    }

    @Bean
    public TheatreClient theatreClient(EnvironmentProperties environmentProperties) {
        return new TheatreClientImpl(environmentProperties.getTheatreBaseUrl());
    }

    @Bean
    public TimeoutProcessor timeoutProcessor(TransactionProperties transactionProperties) {
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(transactionProperties.getTimeoutProcessorThreads());
        return new TimeoutProcessor(scheduledExecutorService);
    }
}
