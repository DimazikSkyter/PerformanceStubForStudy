package ru.nspk.performance.transactionshandler.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record Seat(@JsonProperty("place") String place, @JsonProperty("price") double price, @JsonProperty("userId") String userId) {

    @JsonCreator
    public Seat {
    }
}
