package ru.nspk.performance.theatre.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.nspk.performance.theatre.dto.SeatDto;
import ru.nspk.performance.theatre.dto.SeatResponse;
import ru.nspk.performance.theatre.exception.EventNotFound;
import ru.nspk.performance.theatre.model.Event;
import ru.nspk.performance.theatre.model.SeatStatus;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Service
@RequiredArgsConstructor
public class EventServiceDefault implements EventService {

    private final Map<String, Event> events;

    @Override
    public Set<String> eventNames() {
        return events.keySet();
    }

    @Override
    public SeatResponse seats(String eventName) {
        Set<SeatDto> seatDtoSet = new HashSet<>();
        try {
            seatDtoSet = Optional.ofNullable(events.get(eventName))
                    .orElseThrow(() -> new EventNotFound(eventName))
                    .getSeats()
                    .entrySet()
                    .stream()
                    .filter(entry -> entry.getValue() != null && entry.getValue().seatStatus().equals(SeatStatus.FREE))
                    .map(entry -> new SeatDto(entry.getKey(), entry.getValue().seatStatus(), entry.getValue().price()))
                    .collect(Collectors.toSet());
        } catch (EventNotFound e) {
            return SeatResponse.failed(eventName, "Event not found");
        } catch (Exception e) {
            return SeatResponse.failed(eventName, "Failed to get seats for event");
        }
        if (seatDtoSet.isEmpty()) {
            return SeatResponse.failed(eventName, "No free seats in event");
        }
        return new SeatResponse(eventName, null, seatDtoSet);
    }
}
