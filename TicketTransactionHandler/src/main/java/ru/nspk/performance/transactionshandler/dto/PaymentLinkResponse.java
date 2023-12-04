package ru.nspk.performance.transactionshandler.dto;

import java.time.Instant;

public record PaymentLinkResponse(String status, byte[] qrBytes, String requestId, Instant created, Long timeToPayMs, String errorMessage) {
}