package ru.study.stub.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.study.stub.dto.EventDto;
import ru.study.stub.dto.EventResponse;
import ru.study.stub.dto.SeatDto;
import ru.study.stub.dto.SeatResponse;
import ru.study.stub.model.Event;
import ru.study.stub.theatre.TheatreClient;
import ru.study.stub.theatre.model.TheatreSeatResponse;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final TheatreClient theatreClient;
    private final Map<String, Event> events = new ConcurrentHashMap();

    @Scheduled(cron = "*/15 * * * *")
    public void updateEvents() {
        try {
            Set<String> eventNames = theatreClient.events();
            if (eventNames.isEmpty()) {
                return;
            }

            eventNames.forEach(name -> {
                if (!events.containsKey(name)) {
                    Event event = theatreClient.info(name);
                    events.put(name, event);
                }
            });
        } catch (Exception e) {
            log.error("Get error while update list of exists events", e);
        }
    }

    @Override
    public Duration getTimeToPay(Event event) {
        return Duration.of(10, ChronoUnit.MINUTES);
    }

    @Override
    public EventResponse getEventsByMerchantAndDate(String merchant, Date date) {
        try {
            return EventResponse.success(events.values().stream()
                    .filter(event -> event.getDate().equals(date) && event.getMerchant().equals(merchant))
                    .map(event -> new EventDto(event.getEventName(), event.getDate()))
                    .collect(Collectors.toList()));
        } catch (Exception e) {
            log.error("Failed to get event info", e);
        }
        return EventResponse.error("Failed to get info about event");
    }

    @Override
    public SeatResponse getSeatsFromEvent(@NonNull String eventName) {
        try {
            Event event = Optional.ofNullable(events.get(eventName)).orElseGet(
                    () -> {
                        Event info = theatreClient.info(eventName);
                        events.put(eventName, info);
                        return info;
                    }
            );
            if (event == null) {
                return SeatResponse.error("Event " + eventName + " not found");
            }

            TheatreSeatResponse response = theatreClient.seats(eventName);
            if (response.errorMessage() != null) {
                return SeatResponse.error("Failed to get seats for event " + eventName);
            }

            return SeatResponse.success(response.seats().stream()
                    .map(theatreSeatDto -> new SeatDto(
                            theatreSeatDto.place(),
                            theatreSeatDto.price()
                    )).collect(Collectors.toList()));

        } catch (Exception e) {
            log.error("Failed to get seats from event {}", eventName, e);
        }
        return SeatResponse.error("Failed to get seats for event " + eventName+ ". Unknown error.");
    }
}
