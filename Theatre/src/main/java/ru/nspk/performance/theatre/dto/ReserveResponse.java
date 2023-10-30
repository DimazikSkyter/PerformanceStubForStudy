package ru.nspk.performance.theatre.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReserveResponse {

    private long reserveId;
    private Long requestId;
    private Instant reserveStarted;
    private Set<String> nonFreeSeats;
    @Builder.Default
    private String reserveDuration = "PT5M";
    private String errorMessage;
}
