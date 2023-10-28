package ru.nspk.performance.theatre.dto;

import ru.nspk.performance.theatre.model.SeatStatus;

public record SeatDto(String place, SeatStatus seatStatus, double price) {}