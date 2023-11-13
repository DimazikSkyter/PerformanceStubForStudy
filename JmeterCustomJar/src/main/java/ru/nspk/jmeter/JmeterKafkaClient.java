package ru.nspk.jmeter;

import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class JmeterKafkaClient extends AbstractJavaSamplerClient {

    String topicName = "new_transaction_requests";

    @Override
    public SampleResult runTest(JavaSamplerContext context) {

        SampleResult result = new SampleResult();
        boolean success = true;
        result.sampleStart();

        String row = context.getParameter("row");

        Properties propsProducer = new Properties();
        propsProducer.put("bootstrap.servers", "localhost:9092");
        propsProducer.put("acks", "all");
        propsProducer.put("retries", 0);
        propsProducer.put("batch.size", 16384);
        propsProducer.put("linger.ms", 1);
        propsProducer.put("buffer.memory", 33554432);
        propsProducer.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        propsProducer.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        Producer<String, String> producer = new KafkaProducer<>(propsProducer);

        Properties propsConsumer = new Properties();
        propsConsumer.put("bootstrap.servers", "localhost:9092");
        propsConsumer.put("group.id", "test");
        propsConsumer.put("enable.auto.commit", "true");
        propsConsumer.put("auto.commit.interval.ms", "1000");
        propsConsumer.put("session.timeout.ms", "30000");
        propsConsumer.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        propsConsumer.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        Consumer<String, String> consumer = new KafkaConsumer<>(propsConsumer);

        try {
            producer.send(new ProducerRecord<>(topicName, row)).get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Send succesful");
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        consumer.subscribe(Collections.singletonList(topicName));

        StringBuilder stringBuilder = new StringBuilder("");
        for(int i = 0; i < 10; i++) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.of(150, ChronoUnit.MILLIS));
            for (ConsumerRecord<String, String> record : records) {
                stringBuilder.append(record.value());
                System.out.println("Get from kafka value " + record.value());
            }
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        producer.close();
        consumer.close();
        result.setResponseData(stringBuilder.toString(), "UTF-8");
        result.sampleEnd();
        result.setSuccessful(success);
        return result;
    }
}
