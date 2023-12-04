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
import ru.nspk.performance.action.PaymentLinkResponseAction;
import ru.nspk.performance.action.PaymentResultAction;
import ru.nspk.performance.api.TicketRequest;
import ru.nspk.performance.keyvaluestorage.HazelcastKeyValue;
import ru.nspk.performance.keyvaluestorage.HazelcastManager;
import ru.nspk.performance.keyvaluestorage.KeyValuePortableFactory;
import ru.nspk.performance.keyvaluestorage.KeyValueStorage;
import ru.nspk.performance.keyvaluestorage.model.Person;
import ru.nspk.performance.transactionshandler.dto.PaymentLinkResponse;
import ru.nspk.performance.transactionshandler.dto.PaymentOrderResult;
import ru.nspk.performance.transactionshandler.model.PaymentCheckResponse;
import ru.nspk.performance.action.ReserveResponseAction;
import ru.nspk.performance.transactionshandler.payment.PaymentClient;
import ru.nspk.performance.transactionshandler.payment.PaymentClientImpl;
import ru.nspk.performance.transactionshandler.properties.*;
import ru.nspk.performance.transactionshandler.service.TransactionalEventService;
import ru.nspk.performance.transactionshandler.state.TicketTransactionState;
import ru.nspk.performance.transactionshandler.theatreclient.TheatreClient;
import ru.nspk.performance.transactionshandler.theatreclient.TheatreClientImpl;
import ru.nspk.performance.transactionshandler.theatreclient.dto.ReserveResponse;
import ru.nspk.performance.transactionshandler.timeoutprocessor.TimeoutProcessor;
import ru.nspk.performance.transactionshandler.transformer.*;
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
        transformers.put(PaymentResultAction.class, new PaymentResultActionTransformer());
        transformers.put(PaymentLinkResponse.class, new PaymentLinkResponseActionTransformer());
        transformers.put(PaymentOrderResult.class, new PaymentOrderResultTransformer());
        transformers.put(ReserveResponse.class, new ReserveResponseTransformer());
        transformers.put(ReserveResponseAction.class, new PaymentLinkActionTransformer());
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
                //totalAmount
        );

        List<Pair<String, String>> paymentOrderPatterns = List.of(
                Pair.of("requestId", "\"requestId\"\\s*:\\s*\".+\""),
                Pair.of("status", "\"status\"\\s*:\\s*\"(Success|Failed)\"")
        );

        List<Pair<String, String>> paymentLinkPatterns = List.of(
                Pair.of("status", "\"status\"\\s*:\\s*\"Created\""),
                Pair.of("created", "\"created\"\\s*:\\s*\".+\""),
                Pair.of("qrBytes", "\"qrBytes\"\\s*:\\s*\".+\"")
        );

        inputValidators.put(PaymentLinkResponse.class, new InputValidator(paymentLinkPatterns));
        inputValidators.put(PaymentOrderResult.class, new InputValidator(paymentOrderPatterns));
        inputValidators.put(PaymentCheckResponse.class, new InputValidator(paymentCheckResponsePatterns));
        inputValidators.put(ReserveResponseAction.class, new InputValidator(reserveResponseActionPatterns));
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
    public PaymentClient paymentClient(EnvironmentProperties environmentProperties,
                                       PaymentClientProperties paymentClientProperties) {
        return new PaymentClientImpl(environmentProperties.getPaymentServiceBaseUrl(), paymentClientProperties);
    }

    @Bean
    public TimeoutProcessor timeoutProcessor(TransactionProperties transactionProperties) {
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(transactionProperties.getTimeoutProcessorThreads());
        return new TimeoutProcessor(scheduledExecutorService);
    }
}
