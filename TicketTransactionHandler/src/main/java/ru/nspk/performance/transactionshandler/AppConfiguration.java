package ru.nspk.performance.transactionshandler;


import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.nspk.performance.api.TicketRequest;
import ru.nspk.performance.transactionshandler.keyvaluestorage.HazelcastKeyValue;
import ru.nspk.performance.transactionshandler.keyvaluestorage.KeyValueStorage;
import ru.nspk.performance.transactionshandler.model.PaymentCheckResponse;
import ru.nspk.performance.events.ReserveResponseAction;
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
import java.util.List;
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
    public ValidatorMultiton validatorMultiton(KeyValueStorage keyValueStorage,
                                               TheatreClient theatreClient) {
        Map<Class, InputValidator> inputValidators = new HashMap<>();

        //todo добавить в валидация сверку отправителя
        List<Pair<String, String>> paymentCheckResponsePatterns = List.of(
                Pair.of("request_id", "\"request_id\"\\s*:\\s*\\d+"),
                Pair.of("payment_status", "\"payment_status\"\\s*:\\s*(true|false)")
        );

        List<Pair<String, String>> reserveResponseActionPatterns = List.of(
                Pair.of("request_id", "\"request_id\"\\s*:\\s*\\d+"),
                Pair.of("reserve_id", "\"reserve_id\"\\s*:\\s*(true|false)")
        );

        inputValidators.put(PaymentCheckResponse.class, new PaymentCheckResponseInputValidator(paymentCheckResponsePatterns));
        inputValidators.put(ReserveResponseAction.class, new ReserveResponseEventInputValidator(reserveResponseActionPatterns));
        Map<Class, ModelValidator> modelValidators = new HashMap<>();
        modelValidators.put(TicketRequest.class, new TicketRequestInputValidator(keyValueStorage, theatreClient));
        return new ValidatorMultiton(inputValidators, modelValidators);
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
        maps.put(TransactionalEventService.REQUESTS_MAP, new HazelcastKeyValue(TransactionalEventService.REQUESTS_MAP, requestsMap, inMemoryProperties));

        IMap<Long, TicketTransactionState> transactionStateMap = hazelcastInstance.getMap(TransactionalEventService.TRANSACTIONS_MAP);
        maps.put(TransactionalEventService.TRANSACTIONS_MAP, new HazelcastKeyValue(TransactionalEventService.TRANSACTIONS_MAP, transactionStateMap, inMemoryProperties));
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
