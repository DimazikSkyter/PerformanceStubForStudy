package ru.nspk.performance.transactionshandler;


import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.nio.serialization.ClassDefinition;
import com.hazelcast.nio.serialization.ClassDefinitionBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.nspk.performance.api.TicketRequest;
import ru.nspk.performance.keyvaluestorage.HazelcastKeyValue;
import ru.nspk.performance.keyvaluestorage.HazelcastManager;
import ru.nspk.performance.keyvaluestorage.KeyValuePortableFactory;
import ru.nspk.performance.keyvaluestorage.KeyValueStorage;
import ru.nspk.performance.keyvaluestorage.model.Person;
import ru.nspk.performance.transactionshandler.model.PaymentCheckResponse;
import ru.nspk.performance.events.ReserveResponseAction;
import ru.nspk.performance.transactionshandler.properties.EnvironmentProperties;
import ru.nspk.performance.transactionshandler.properties.InMemoryProperties;
import ru.nspk.performance.transactionshandler.properties.TheatreClientProperties;
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

@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
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
    public KeyValueStorage keyValueStorage(Map<String, HazelcastKeyValue> keyValueMap) {
        return new HazelcastManager(keyValueMap);
    }

    @Bean
    public ValidatorMultiton validatorMultiton(KeyValueStorage keyValueStorage,
                                               TheatreClient theatreClient) {
        Map<Class, InputValidator> inputValidators = new HashMap<>();

        //todo добавить в валидация сверку отправителя
        List<Pair<String, String>> paymentCheckResponsePatterns = List.of(
                Pair.of("requestId", "\"requestId\"\\s*:\\s*\\d+"),
                Pair.of("paymentStatus", "\"paymentStatus\"\\s*:\\s*(true|false)")
        );

        List<Pair<String, String>> reserveResponseActionPatterns = List.of(
                Pair.of("reserveId", "\"reserveId\"\\s*:\\s*[0-9]+")
                //reserveStarted
                //reserveDuration
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
        ClassDefinition classDefinition = new ClassDefinitionBuilder(KeyValuePortableFactory.FACTORY_ID, Person.ID)
                .addStringField("fio")
                .addStringField("passportCode")
                .addStringField("address")
                .addIntField("version")
                .addIntField("age")
                .build();
        clientConfig.getSerializationConfig().addClassDefinition(classDefinition);
        clientConfig.getSerializationConfig().addPortableFactory(KeyValuePortableFactory.FACTORY_ID, new KeyValuePortableFactory());
        clientConfig.getNetworkConfig().addAddress(inMemoryProperties.getAddress());
        return clientConfig;
    }

    @Bean
    public Map<String, HazelcastKeyValue> keyValueMap(HazelcastInstance hazelcastInstance,
                                                      InMemoryProperties inMemoryProperties) {
        Map<String, HazelcastKeyValue> map = new HashMap<>();
        IMap<String, Long> requestsMap = hazelcastInstance.getMap(TransactionalEventService.REQUESTS_MAP);
        map.put(TransactionalEventService.REQUESTS_MAP, new HazelcastKeyValue(TransactionalEventService.REQUESTS_MAP,
                requestsMap,
                inMemoryProperties.getTimeToLive(),
                inMemoryProperties.getLockTimeoutMs(),
                inMemoryProperties.getTimeout()));

        IMap<Long, TicketTransactionState> transactionStateMap = hazelcastInstance.getMap(TransactionalEventService.TRANSACTIONS_MAP);
        map.put(TransactionalEventService.TRANSACTIONS_MAP, new HazelcastKeyValue(TransactionalEventService.TRANSACTIONS_MAP, transactionStateMap,
                inMemoryProperties.getTimeToLive(),
                inMemoryProperties.getLockTimeoutMs(),
                inMemoryProperties.getTimeout()));

        IMap<Long, Person> personsMap = hazelcastInstance.getMap(TransactionalEventService.PERSON_DATA_MAP);
        map.put(TransactionalEventService.PERSON_DATA_MAP, new HazelcastKeyValue(TransactionalEventService.PERSON_DATA_MAP,
                personsMap,
                inMemoryProperties.getTimeToLive(),
                inMemoryProperties.getLockTimeoutMs(),
                inMemoryProperties.getTimeout()));
        return map;
    }

    @Bean
    public TheatreClient theatreClient(EnvironmentProperties environmentProperties,
                                       TheatreClientProperties theatreClientProperties) {
        return new TheatreClientImpl(environmentProperties.getTheatreBaseUrl(), theatreClientProperties);
    }

    @Bean
    public TimeoutProcessor timeoutProcessor(TransactionProperties transactionProperties) {
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(transactionProperties.getTimeoutProcessorThreads());
        return new TimeoutProcessor(scheduledExecutorService);
    }
}
