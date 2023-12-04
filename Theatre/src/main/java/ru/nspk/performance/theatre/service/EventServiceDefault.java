package ru.nspk.performance.theatre.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import ru.nspk.performance.theatre.dto.CreateEventRequest;
import ru.nspk.performance.theatre.dto.EventDto;
import ru.nspk.performance.theatre.dto.SeatDto;
import ru.nspk.performance.theatre.dto.SeatResponse;
import ru.nspk.performance.theatre.exception.EventNotFound;
import ru.nspk.performance.theatre.model.Event;
import ru.nspk.performance.theatre.model.Seat;
import ru.nspk.performance.theatre.model.SeatStatus;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Getter
@Service
@RequiredArgsConstructor
public class EventServiceDefault implements EventService {

    private final Random random = new Random();
    private AtomicLong eventSequence = new AtomicLong(1);
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
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

    @Override
    public EventDto eventInfo(String eventName) {
        Event event = events.get(eventName);
        if (event == null) {
            return EventDto.builder().build();
        }
        return EventDto.builder()
                .name(eventName)
                .date(event.getEventDate())
                .type(event.getType())
                .merchant(event.getMerchant())
                .title(event.getName())
                .exists(true)
                .build();
    }

    @Override
    public EventDto createNewEvent(CreateEventRequest createEventRequest) {
        Event event = new Event(createEventRequest.name(), createEventRequest.date(), seats(createEventRequest.seatCount()), createEventRequest.merchant(), createEventRequest.type());
        String eventName = "generated_event_" + eventSequence.getAndIncrement();
        events.put(eventName, event);
        return EventDto.builder()
                .name(eventName)
                .date(event.getEventDate())
                .type(event.getType())
                .merchant(event.getMerchant())
                .title(event.getName())
                .exists(true)
                .build();
    }

    private Map<String, Seat> seats(int seatsCount) {
        return IntStream.range(0, seatsCount)
                .mapToObj(value -> Pair.of(key(value), new Seat(SeatStatus.FREE, random.nextDouble(100.0))))
                .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
    }

    private String key(int value) {
        int numSeatPart = value / 26;
        return String.valueOf(ALPHABET.charAt(value%26)) + numSeatPart;
    }
}
