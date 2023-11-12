package ru.study.stub.dto;

import java.util.List;

public record TicketPurchaseRequest(EventDto event, List<TicketDto> tickets) {
}
