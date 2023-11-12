package ru.nspk.performance.transactionshandler.model.theatrecontract;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
@Jacksonized
public class Event {

    private Instant eventDate;
    private String eventName;
    private List<String> seats;
}
