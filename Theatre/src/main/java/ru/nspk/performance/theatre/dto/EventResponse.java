package ru.nspk.performance.theatre.dto;

import lombok.Data;

import java.util.List;

@Data
public class EventResponse {

    private List<EventDto> events;
    private String errorMessage;
}