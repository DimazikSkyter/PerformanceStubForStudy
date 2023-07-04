package ru.nspk.performance.theatre.model;

import lombok.Data;
import ru.nspk.performance.theatre.exception.SeatsAlreadySoldException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Data
public class Event {

    private String name;
    private Map<String, SeatStatus> seats;

    private Object syncReleaseAndPurchase = new Object();

    public synchronized List<String> reserveAll(List<String> seatsToReserve) {
        return null;
    }

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
