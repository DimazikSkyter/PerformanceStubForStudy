package ru.study.api;

import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.nio.serialization.ClassDefinition;
import com.hazelcast.nio.serialization.ClassDefinitionBuilder;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.http.HttpClientTransportOverHTTP;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.JettyClientHttpConnector;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.reactive.function.client.WebClient;
import ru.nspk.performance.keyvaluestorage.hazelcast.HazelcastKeyValue;
import ru.nspk.performance.keyvaluestorage.hazelcast.HazelcastManager;
import ru.nspk.performance.keyvaluestorage.KeyValuePortableFactory;
import ru.nspk.performance.keyvaluestorage.model.Person;
import ru.study.api.producer.KafkaProducerImpl;
import ru.study.api.producer.QueueProducer;
import ru.study.api.properties.InMemoryProperties;
import ru.study.api.properties.TheatreProperty;

import java.util.HashMap;
import java.util.Map;

import static ru.study.api.service.UserServiceImpl.PASSPORT_PERSON_MAP;
import static ru.study.api.service.UserServiceImpl.PERSON_DATA_MAP;


@Configuration
@EnableScheduling
public class TicketStoreConfigurator {

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

    @Bean Map<String, HazelcastKeyValue> hazelcastKeyValues(HazelcastInstance hazelcastInstance,
                                                            InMemoryProperties inMemoryProperties) {
        Map<String, HazelcastKeyValue> map = new HashMap<>();
        IMap<String, Person> personMap = hazelcastInstance.getMap(PERSON_DATA_MAP);
        map.put(PERSON_DATA_MAP, new HazelcastKeyValue(PERSON_DATA_MAP,
                personMap,
                inMemoryProperties.getTimeToLive(),
                inMemoryProperties.getLockTimeoutMs(),
                inMemoryProperties.getTimeout()));


        IMap<String, String> passportMap = hazelcastInstance.getMap(PASSPORT_PERSON_MAP);
        map.put(PASSPORT_PERSON_MAP, new HazelcastKeyValue(PASSPORT_PERSON_MAP,
                passportMap,
                inMemoryProperties.getTimeToLive(),
                inMemoryProperties.getLockTimeoutMs(),
                inMemoryProperties.getTimeout()));


        return map;
    }

    @Bean
    public HazelcastManager hazelcastManager(Map<String, HazelcastKeyValue> hazelcastKeyValues) {
        return new HazelcastManager(hazelcastKeyValues);
    }

    @Bean
    public WebClient theatreClient(TheatreProperty theatreProperty) {
        return WebClient.builder().baseUrl(theatreProperty.getBaseUrl()).clientConnector(
                new JettyClientHttpConnector(new HttpClient(new HttpClientTransportOverHTTP()))
        ).build();
    }

    @Bean
    public QueueProducer kafkaProducer(KafkaTemplate<Long, byte[]> kafkaTemplate) {
        return new KafkaProducerImpl("ticket_request", kafkaTemplate);
    }
}
