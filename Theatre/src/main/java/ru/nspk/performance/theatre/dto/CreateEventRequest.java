package ru.nspk.performance.theatre.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CreateEventRequest(String name, @JsonFormat(pattern="yyyy-MM-dd", timezone = "Europe/Moscow") Date date, int seatCount, String merchant, String type) {
}
