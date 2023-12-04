package ru.study.processing.dto;

import ru.nspk.performance.qr.QrData;

public class PaymentDetailsDto {

    private String purpose;
    private double amount;
    private String account;

    public QrData toQrData() {
        return QrData.builder()
                .purpose(purpose)
                .amount(amount)
                .targetAccount(account)
                .build();
    }
}
