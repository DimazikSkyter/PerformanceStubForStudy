package ru.nspk.jmeter;

import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import ru.nspk.performance.api.PaymentLinkToApi;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Properties;

public class PaymentLinkExtractorConsumer extends AbstractJavaSamplerClient {

    private final String topicName = "payment_link";
    private final PaymentPayClient paymentPayClient = new PaymentPayClient("localhost:8087/processing/payment/pay");
    public boolean exit = false;

    @Override
    public SampleResult runTest(JavaSamplerContext context) {

        SampleResult result = new SampleResult();
        boolean success = true;
        result.sampleStart();

        Consumer<String, byte[]> consumer = consumer();

        consumer.subscribe(Collections.singletonList(topicName));
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                consumer.close();
            } catch (Exception ignored) {}
        }));

        while (true) {
            ConsumerRecords<String, byte[]> records = consumer.poll(Duration.of(500, ChronoUnit.MILLIS));
            for (ConsumerRecord<String, byte[]> record : records) {
                try {
                    PaymentLinkToApi paymentLinkToApi = PaymentLinkToApi.parseFrom(record.value());
                    String requestId = paymentLinkToApi.getPaymentLinkRequestId();
                    if (requestId != null)
                        System.out.println("New payment request with id " + requestId);
                        paymentPayClient.pay(requestId);
                } catch (InvalidProtocolBufferException e) {
                    System.out.println("Failed to parse payment link to api");
                }
            }
            if (exit) {
                break;
            }
        }

        consumer.close();

        result.sampleEnd();
        result.setSuccessful(success);
        return result;
    }

    private Consumer<String, byte[]> consumer() {
        Properties propsConsumer = new Properties();
        propsConsumer.put("bootstrap.servers", "localhost:9092");
        propsConsumer.put("group.id", "test");
        propsConsumer.put("enable.auto.commit", "true");
        propsConsumer.put("auto.commit.interval.ms", "1000");
        propsConsumer.put("session.timeout.ms", "30000");
        propsConsumer.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        propsConsumer.put("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");

        return new KafkaConsumer<>(propsConsumer);
    }
}
