package ru.study.processing.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.nspk.performance.qr.QrData;
import ru.nspk.performance.qr.QrStreamDecorator;
import ru.study.processing.dto.PaymentDetailsDto;
import ru.study.processing.model.PaymentLink;

import java.io.DataOutputStream;

@RequiredArgsConstructor
@Slf4j
public class PaymentLinkGeneratorServiceImpl implements PaymentLinkGeneratorService {

    private final QrStreamDecorator qrStreamDecorator;
    private final String account;

    @Override
    public PaymentLink generate(PaymentDetailsDto paymentDetailsDto) {
        QrData qrData = paymentDetailsDto.toQrData(account);
        //persist to kafka  qrData
        try (DataOutputStream qrOutputStream = qrStreamDecorator.createQrOutputStream(qrData)) {
            byte[] bytes = new byte[qrOutputStream.size()];
            qrOutputStream.write(bytes);
            return PaymentLink.builder()
                    .status("Created")
                    .qrBytes(bytes)
                    .build();
        } catch (Exception e) {
            //todo нужно линковать ошибочные qr
            return PaymentLink.builder()
                    .status("Failed to create.")
                    .build();
        }
    }
}
