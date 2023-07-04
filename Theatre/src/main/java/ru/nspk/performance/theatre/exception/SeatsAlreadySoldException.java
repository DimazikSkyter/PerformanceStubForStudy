package ru.nspk.performance.theatre.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class SeatsAlreadySoldException extends RuntimeException {

    @Getter
    private final List<String> seats;
}
