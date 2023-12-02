package ru.nspk.performance.transactionshandler.model.theatrecontract;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.extern.jackson.Jacksonized;
import ru.nspk.performance.transactionshandler.model.Seat;

import java.io.Serializable;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
@Jacksonized
public class Event {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @NonNull private Date eventDate;
    private String eventName;
    private List<Seat> seats;
}
