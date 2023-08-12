package ru.nspk.performance.transactionshandler.schemaregistry;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;

public class SchemaRegistryContainer extends GenericContainer<SchemaRegistryContainer> {
    public static final String SCHEMA_REGISTRY_IMAGE =
            "confluentinc/cp-schema-registry";
    public static final int SCHEMA_REGISTRY_PORT = 8081;

    public SchemaRegistryContainer(String version) {
        super(SCHEMA_REGISTRY_IMAGE + ":" + version);

        waitingFor(Wait.forHttp("/subjects").forStatusCode(200));
        withExposedPorts(SCHEMA_REGISTRY_PORT);
    }

    public SchemaRegistryContainer withKafka(KafkaContainer kafka) {
        return withKafka(kafka.getNetwork(), "172.18.0.2" + ":" + "2181");
    }

    public SchemaRegistryContainer withKafka(Network network, String kafka) {
        withNetwork(network);
        withEnv("SCHEMA_REGISTRY_HOST_NAME", "schemaregistry");
        withEnv("SCHEMA_REGISTRY_LISTENERS", "http://0.0.0.0:8081");
//        withEnv("SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS",kafka);
        withEnv("SCHEMA_REGISTRY_KAFKASTORE_CONNECTION_URL", kafka);
        return self();
    }

}
