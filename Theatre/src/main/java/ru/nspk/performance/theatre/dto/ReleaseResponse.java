package ru.nspk.performance.theatre.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ReleaseResponse(String message, long reserveId, String errorMessage) {

    public static ReleaseResponse success(long reserveId) {
        return new ReleaseResponse("Release canceled.", reserveId, null);
    }

    public static ReleaseResponse failed(long reserveId, String reason) {
        return new ReleaseResponse(null, reserveId, "Failed to cancel reserve for reason.\n" + reason);
    }
}
