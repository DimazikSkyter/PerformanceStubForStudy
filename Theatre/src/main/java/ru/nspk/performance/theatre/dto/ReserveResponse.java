package ru.nspk.performance.theatre.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

@Data
@Builder()
@NoArgsConstructor
@AllArgsConstructor
public class ReserveResponse {

    private long reserveId;
    private long requestId;
    private Instant reserveStarted;
    private Set<String> nonFreeSeats;
    @Builder.Default
    private String reserveDuration = "PT5M";
    private String errorMessage;
}
