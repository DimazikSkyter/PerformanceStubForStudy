package ru.study.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import ru.study.api.model.TicketStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TicketCheckResponse(String uid, String status, String errorMessage) {

    public static TicketCheckResponse of(String uid, TicketStatus ticketStatus) {
        if (validateStatus(ticketStatus.status())) {
            return new TicketCheckResponse(uid, ticketStatus.status(), null);
        }
        return new TicketCheckResponse(uid, ticketStatus.status(), "Invalid status");
    }

    public static TicketCheckResponse uidNotFound(String uid) {
        return new TicketCheckResponse(uid, null, "Request wasn't found.");
    }

    public static TicketCheckResponse error(String uid) {
        return new TicketCheckResponse(uid, null, "Failed to check request, please, try to contact support.");
    }

    private static boolean validateStatus(String status) {
        return false;
    }
}
