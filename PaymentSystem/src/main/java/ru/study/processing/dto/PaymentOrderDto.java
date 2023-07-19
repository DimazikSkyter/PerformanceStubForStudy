package ru.study.processing.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentOrderDto {

    private String secretKey;
    private String secretPaymentIdentification;
}
