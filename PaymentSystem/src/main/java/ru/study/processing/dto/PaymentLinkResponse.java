package ru.study.processing.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentLinkResponse {

    private String status;
    private byte[] qrBytes;
    private String requestId;
    private String errorMessage;
    private Instant created;
    private long timeToPayMs;
}
