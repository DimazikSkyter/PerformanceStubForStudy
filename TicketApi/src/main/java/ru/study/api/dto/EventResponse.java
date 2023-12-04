package ru.study.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record EventResponse(String errorMessage, List<EventDto> events) {

    public static EventResponse success(@NonNull List<EventDto> events) {
        if (events.isEmpty()) {
            return new EventResponse("Events not found", new ArrayList<>());
        }
        return new EventResponse(null, events);
    }

    public static EventResponse error(String errorMessage) {
        return new EventResponse(errorMessage, null);
    }
}
