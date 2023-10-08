package ru.nspk.performance.theatre.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import ru.nspk.performance.theatre.exception.SeatsAlreadySoldException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Data
@RequiredArgsConstructor(onConstructor = @__(@JsonCreator))
public class Event {

    @JsonProperty("name")
    private final String name;
    @JsonProperty("seats")
    private final Map<String, SeatStatus> seats;

    @JsonIgnore
    private Object syncReleaseAndPurchase = new Object();


    public List<String> releaseAll(List<String> seatsToRelease) {
        synchronized (syncReleaseAndPurchase) {
            List<String> unreleasedSeats = new ArrayList<>();
            seatsToRelease.forEach(
                    seat -> {
                        releaseSeat(seat)
                                .ifPresent(unreleasedSeats::add);
                    }
            );
            return unreleasedSeats;
        }
    }

    public void purchase(List<String> seatsToPurchase) {
        synchronized (syncReleaseAndPurchase) {
            List<String> alreadySold = seatsToPurchase.stream().filter(seat -> seats.get(seat).equals(SeatStatus.RESERVED)).toList();
            if (alreadySold.isEmpty()) {
                seatsToPurchase.forEach(seat -> seats.put(seat, SeatStatus.BOUGHT));
            } else {
                throw new SeatsAlreadySoldException(alreadySold);
            }
        }
    }

    private Optional<String> releaseSeat(String seat) {
        SeatStatus seatStatus = seats.get(seat);
        if (seatStatus.equals(SeatStatus.RESERVED)) {
            seats.put(seat, SeatStatus.FREE);
            return Optional.empty();
        }
        return Optional.of(seat);
    }
}
