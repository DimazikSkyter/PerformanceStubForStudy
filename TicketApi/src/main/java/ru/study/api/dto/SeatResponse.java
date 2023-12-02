package ru.study.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SeatResponse(String eventName, String errorMessage, Set<SeatDto> seats) {

    public static SeatResponse error(String eventName, String errorMessage) {
        return new SeatResponse(eventName, errorMessage, null);
    }
}
