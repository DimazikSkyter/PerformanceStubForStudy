package ru.study.processing.exception;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AlreadyPaidException extends RuntimeException {

    private final String uid;
}
