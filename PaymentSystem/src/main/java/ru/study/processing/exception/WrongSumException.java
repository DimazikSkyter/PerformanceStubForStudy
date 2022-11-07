package ru.study.processing.exception;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class WrongSumException extends RuntimeException {

    private final String uid;
    private final double price;
}
