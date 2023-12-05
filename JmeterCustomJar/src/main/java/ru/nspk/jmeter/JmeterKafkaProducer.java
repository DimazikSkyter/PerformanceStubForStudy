package ru.nspk.jmeter;

import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

public class JmeterKafkaProducer extends AbstractJavaSamplerClient {
    @Override
    public SampleResult runTest(JavaSamplerContext context) {
        return null;
    }
}
