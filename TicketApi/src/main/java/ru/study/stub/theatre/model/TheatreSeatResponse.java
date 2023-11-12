package ru.study.stub.theatre.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TheatreSeatResponse(String eventName, String errorMessage, Set<TheatreSeatDto> seats){}