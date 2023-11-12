package ru.study.stub.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SeatResponse(String errorMessage, List<SeatDto> seats) {

    public static SeatResponse success(List<SeatDto> seats) {
        if (seats.isEmpty()) {
            return new SeatResponse("No free seats in event", new ArrayList<>());
        }
        return new SeatResponse(null, seats);
    }

    public static SeatResponse error(String errorMessage) {
        return new SeatResponse(errorMessage, null);
    }
}
