package ru.nspk.performance.transactionshandler.processor;

import lombok.Builder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.nspk.performance.transactionshandler.KafkaConfig;

import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {TicketEventProcessorTest.Config.class})
@ExtendWith(SpringExtension.class)
@ActiveProfiles("kafka")
@Slf4j
class TicketEventProcessorTest {

    @Autowired
    private TicketEventProcessor ticketEventProcessor;

    @Autowired
    private StreamsBuilder streamsBuilder;

    @TestConfiguration
    public static class Config {
        @Bean
        public StreamsBuilder streamsBuilder() {
            return new StreamsBuilder();
        }

    }

    @SneakyThrows
    @Test
    void givenInputMessages_whenProcessed_thenWordCountIsProduced() {

        Topology topology = streamsBuilder.build();

        try (TopologyTestDriver topologyTestDriver = new TopologyTestDriver(topology, new Properties())) {
            TestInputTopic<String, String> inputTopic = topologyTestDriver
                    .createInputTopic("input-topic", new StringSerializer(), new StringSerializer());

            TestOutputTopic<String, Long> outputTopic = topologyTestDriver
                    .createOutputTopic("output-topic", new StringDeserializer(), new LongDeserializer());

            inputTopic.pipeInput("key", "hello world");
            inputTopic.pipeInput("key2", "hello");

            List<KeyValue<String, Long>> keyValues = outputTopic.readKeyValuesToList();

            for (KeyValue<String, Long> kv : keyValues) {
                log.info("Get new kv {}:{}", kv.key, kv.value);
            }

            assertThat(keyValues)
                    .containsExactly(
                            KeyValue.pair("hello", 1L),
                            KeyValue.pair("world", 1L),
                            KeyValue.pair("hello", 2L)
                    );
        }
    }
}