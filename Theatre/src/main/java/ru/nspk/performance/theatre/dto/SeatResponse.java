package ru.nspk.performance.theatre.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SeatResponse(String eventName, String errorMessage, Set<SeatDto> seats) {

    public static SeatResponse success(String eventName, Set<SeatDto> seats) {
        return new SeatResponse(eventName, null, seats);
    }

    public static SeatResponse failed(String eventName, String errorMessage) {
        return new SeatResponse(eventName, errorMessage, null);
    }
}
