package ru.study.api.dto;

import java.util.List;

public record TicketPurchaseRequest(String eventName, List<TicketDto> tickets) {
}
