package ru.study.processing.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.stereotype.Service;
import ru.nspk.performance.qr.QrData;
import ru.nspk.performance.qr.QrStreamDecorator;
import ru.study.processing.dto.PaymentDetailsDto;
import ru.study.processing.dto.PaymentLinkResponse;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.Instant;

@RequiredArgsConstructor
@Slf4j
@Service
public class PaymentLinkGeneratorServiceImpl implements PaymentLinkGeneratorService {

    private final QrStreamDecorator qrStreamDecorator;
    private long timeToPay = 5000000; //todo не проверяется

    @Override
    public PaymentLinkResponse generate(String requestId, PaymentDetailsDto paymentDetailsDto) {
        QrData qrData = paymentDetailsDto.toQrData();
        //persist to kafka  qrData
        try {
            ByteArrayOutputStream qrOutputStream = qrStreamDecorator.createQrOutputStream(qrData);
            return PaymentLinkResponse.builder()
                    .status("Created")
                    .requestId(requestId)
                    .qrBytes(Base64.encodeBase64(qrOutputStream.toByteArray()))
                    .created(Instant.now())
                    .timeToPayMs(timeToPay)
                    .build();
        } catch (Exception e) {
            return PaymentLinkResponse.builder()
                    .status("Failed to create.")
                    .requestId(requestId)
                    .build();
        }
    }
}
