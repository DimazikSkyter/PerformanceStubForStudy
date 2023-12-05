package ru.study.api.dto;

public record PaymentLinkResponse(String errorMessage) {

    public static PaymentLinkResponse error(String errorMessage) {
        return new PaymentLinkResponse(errorMessage);
    }

    public static PaymentLinkResponse success() {
        return new PaymentLinkResponse(null);
    }
}
