package ru.study.api.theatre.model;


public record TheatreSeatDto(String place, TheatreSeatStatus seatStatus, double price) {}