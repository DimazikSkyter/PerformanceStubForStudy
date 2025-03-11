package ru.nspk.performance.keyvaluestorage.cassandra;

import com.datastax.dse.driver.api.core.graph.predicates.CqlCollection;
import com.datastax.oss.driver.api.core.CqlSession;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;


@Testcontainers
class CassandraManagerTest {

    private static final CassandraContainer<?> cassandra = new CassandraContainer<>("cassandra:4.0")
            .withExposedPorts(9042);
    private static CqlSession cqlSession;
    private static CassandraKeyValue<String, String> keyValueString;
    private static CassandraKeyValue<String, Long> keyValueLong;

    @BeforeAll
    static void setUp() throws URISyntaxException, IOException {
        cassandra.start();

        cqlSession = CqlSession.builder()
                .addContactPoint(new InetSocketAddress(cassandra.getHost(), cassandra.getFirstMappedPort()))
                .withLocalDatacenter("datacenter1")
                .build();

        List<String> cqlStatements = Files.readAllLines(Paths.get(
                CassandraManagerTest.class.getClassLoader().getResource("schema.cql").toURI()));

        cqlStatements.forEach(System.out::println);

        StringBuilder statementBuilder = new StringBuilder();
        for (String line : cqlStatements) {
            line = line.trim();
            if (!line.isEmpty()) {
                statementBuilder.append(line).append(" ");
                if (line.endsWith(";")) {
                    String fullStatement = statementBuilder.toString();
                    System.out.println("Executing CQL: " + fullStatement);
                    cqlSession.execute(fullStatement);
                    statementBuilder.setLength(0); // Reset for the next statement
                }
            }
        }

        keyValueString = new CassandraKeyValue<>("test_keyspace.kv_store_str", cqlSession, 3600);
        keyValueLong = new CassandraKeyValue<>("test_keyspace.kv_store_long", cqlSession, 3600);
    }

    @AfterAll
    static void tearDown() {
        cqlSession.close();
        cassandra.stop();
    }


    @Test
    void put() throws JsonProcessingException {
        keyValueString.put("testKey", "testValue", value -> assertEquals("testValue", value));
    }

    @Test
    void get() throws JsonProcessingException {
        keyValueString.put("anotherKey", "anotherValue", value -> {});
        assertEquals("anotherValue", keyValueString.get("anotherKey"));
    }

    @Test
    void getAndIncrement() throws JsonProcessingException {
        String testKey = "counterKey";

        keyValueLong.put(testKey, 0L, value -> {});

        long value1 = keyValueLong.updateWithCondition(testKey, val -> val + 1, val -> true).getValue();
        long value2 = keyValueLong.updateWithCondition(testKey, val -> val + 1, val -> true).getValue();
        long value3 = keyValueLong.updateWithCondition(testKey, val -> val + 1, val -> true).getValue();

        assertEquals(1L, value1);
        assertEquals(2L, value2);
        assertEquals(3L, value3);
    }

    @Test
    void updateWithCondition() throws JsonProcessingException {
        String testKey = "conditionKey";

        keyValueLong.put(testKey, 10L, value -> {});

        long updatedValue = keyValueLong.updateWithCondition(testKey, val -> val + 10, val -> val > 5).getValue();
        long failedUpdate = keyValueLong.updateWithCondition(testKey, val -> val + 10, val -> val > 30).getValue();
        assertEquals(20L, updatedValue);
        assertEquals(20L, failedUpdate);
    }

    @Test
    void getAsync() throws JsonProcessingException, ExecutionException, InterruptedException {
        String testKey = "asyncKey";
        String asyncValue = "async value";
        keyValueString.put(testKey, asyncValue, value -> {});
        CompletableFuture<String> futureValue = keyValueString.getAsync(testKey).toCompletableFuture();
        assertEquals(asyncValue, futureValue.get());
    }
}