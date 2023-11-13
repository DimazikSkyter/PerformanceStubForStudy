package ru.nspk.jmeter;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JmeterKafkaClientTest {

    @Test
    void test() {
        System.out.println("HELLO WORLD");
        JmeterKafkaClient jmeterKafkaClient = new JmeterKafkaClient();
        Arguments arguments = new Arguments();
        arguments.addArgument("row", "ASAD ADSAD");
        JavaSamplerContext javaSamplerContext = new JavaSamplerContext(arguments);

        jmeterKafkaClient.runTest(javaSamplerContext);
    }

}