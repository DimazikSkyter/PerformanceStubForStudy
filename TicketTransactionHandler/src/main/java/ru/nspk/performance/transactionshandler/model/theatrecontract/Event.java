package ru.nspk.performance.transactionshandler.model.theatrecontract;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
public class Event {

    private Instant eventDate;
    private String eventName;
    private List<String> seats;
}
