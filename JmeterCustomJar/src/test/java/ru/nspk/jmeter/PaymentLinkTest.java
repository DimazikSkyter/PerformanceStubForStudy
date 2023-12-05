package ru.nspk.jmeter;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.junit.jupiter.api.Test;

public class PaymentLinkTest {

    @Test
    void handlePaymentLinks() {
        PaymentLinkExtractorConsumer paymentLinkExtractorConsumer = new PaymentLinkExtractorConsumer();

        Arguments arguments = new Arguments();
        JavaSamplerContext javaSamplerContext = new JavaSamplerContext(arguments);

        paymentLinkExtractorConsumer.runTest(javaSamplerContext);
    }
}
