package ru.nspk.performance.transactionshandler.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
@Deprecated
public class TicketEventProcessor {

    private static final Serde<String> STRING_SERDE = Serdes.String();
    private AtomicInteger atomicInteger = new AtomicInteger();

    @Autowired
    public void buildPipeline(StreamsBuilder streamsBuilder) {

        log.info("Current class link {}", ((Object) this).toString());

        int iteration =  atomicInteger.getAndIncrement();
        KStream<String, String> messageStream = streamsBuilder
                .stream("input-topic", Consumed.with(STRING_SERDE, STRING_SERDE));

        KTable<String, Long> wordCounts = messageStream
                .mapValues((ValueMapper<String, String>) String::toLowerCase)
                
//                .process()
//                .flatMapValues(value -> Arrays.asList(value.split("\\W+")))
                .groupBy((key, word) -> word, Grouped.with(STRING_SERDE, STRING_SERDE))
                .count();

        wordCounts.toStream().foreach(
                (key, value) -> log.info("{} Word counts k {}: v {}", iteration, key, value)
        );
        wordCounts.toStream().to("output-topic");
    }

}
