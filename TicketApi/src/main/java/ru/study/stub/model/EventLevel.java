package ru.study.stub.model;

import lombok.Getter;

public enum EventLevel {

    LOW(0.5), MIDDLE(1D), HIGH(2D);

    @Getter
    private final double coeff;

    EventLevel(double coeff) {
        this.coeff = coeff;
    }
}
