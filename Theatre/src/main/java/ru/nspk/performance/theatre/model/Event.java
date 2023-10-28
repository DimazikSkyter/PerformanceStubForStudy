package ru.nspk.performance.theatre.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import ru.nspk.performance.theatre.exception.SeatsAlreadySoldException;

import java.util.*;

//todo #task3 переименовать эвент, чтобы не было дублирование имени
@Data
@RequiredArgsConstructor(onConstructor = @__(@JsonCreator))
public class Event {

    @JsonProperty("name")
    private final String name;
    @JsonFormat(shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd")
    private final Date eventDate;
    @JsonProperty("seats")
    private final Map<String, Seat> seats;

    @JsonIgnore
    private final Object syncReleaseAndPurchase = new Object();


    public List<String> releaseAll(List<String> seatsToRelease) {
        synchronized (syncReleaseAndPurchase) {
            List<String> unreleasedSeats = new ArrayList<>();
            seatsToRelease.forEach(seat -> releaseSeat(seat).ifPresent(unreleasedSeats::add));
            return unreleasedSeats;
        }
    }

    public void purchase(List<String> seatsToPurchase) {
        synchronized (syncReleaseAndPurchase) {
            List<String> alreadySold = seatsToPurchase.stream()
                    .filter(seat -> seats.get(seat).seatStatus().equals(SeatStatus.RESERVED))
                    .toList();
            if (alreadySold.isEmpty()) {
                seatsToPurchase.forEach(seat -> seats.put(seat, new Seat(SeatStatus.BOUGHT, seats.get(seat).price())));
            } else {
                throw new SeatsAlreadySoldException(alreadySold);
            }
        }
    }

    private Optional<String> releaseSeat(String seatName) {
        Seat seat = seats.get(seatName);
        if (seat.seatStatus().equals(SeatStatus.RESERVED)) {
            seats.put(seatName, new Seat(SeatStatus.FREE, seat.price()));
            return Optional.empty();
        }
        return Optional.of(seatName);
    }
}
