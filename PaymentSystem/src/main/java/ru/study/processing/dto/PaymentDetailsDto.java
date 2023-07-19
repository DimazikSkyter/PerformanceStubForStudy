package ru.study.processing.dto;

import ru.nspk.performance.qr.QrData;

public class PaymentDetailsDto {

    private String purpose;
    private int amount;

    public QrData toQrData(String account) {
        return QrData.builder()
                .purpose(purpose)
                .amount(amount)
                .targetAccount(account)
                .build();
    }
}
