package ru.study.stub.dto;


public record TicketPurchaseResponse(boolean status, String errorMessage, String correlationId) {

    public static TicketPurchaseResponse success(String correlationId) {
        return new TicketPurchaseResponse(true, null, correlationId);
    }

    public static TicketPurchaseResponse error(String errorMessage) {
        return new TicketPurchaseResponse(false, errorMessage, null);
    }
}
