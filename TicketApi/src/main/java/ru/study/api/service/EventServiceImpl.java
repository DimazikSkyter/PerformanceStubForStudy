package ru.study.api.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.study.api.dto.EventDto;
import ru.study.api.dto.EventResponse;
import ru.study.api.dto.SeatResponse;
import ru.study.api.model.Event;
import ru.study.api.theatre.TheatreClient;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final TheatreClient theatreClient;
    private final Map<String, Event> events = new ConcurrentHashMap();

    @Scheduled(cron = "*/30 * * * * *")
    public void updateEvents() {
        log.info("Start new cycle of pulling events");
        try {
            Set<String> eventNames = theatreClient.events();
            if (eventNames.isEmpty()) {
                return;
            }

            eventNames.forEach(name -> {
                if (!events.containsKey(name)) {
                    try {
                        Event event = theatreClient.info(name);
                        events.put(name, event);
                    } catch (Exception e) {
                        log.error("Failed to actualize information about event {}", name, e);
                    }
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
    public Event getEventByName(String eventName) {
        return events.get(eventName);
    }

    @Override
    public EventResponse getEventsByMerchantAndDate(String merchant, Date date) {
        try {
            return EventResponse.success(events.values().stream()
                    .filter(event -> DateUtils.isSameDay(event.getDate(), date) && (merchant == null || merchant.equals(event.getMerchant())))
                    .map(event -> new EventDto(event.getTitle(), event.getName(), event.getDate()))
                    .toList());
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
                        Event info = null;
                        try {
                            info = theatreClient.info(eventName);
                            events.put(eventName, info);
                        } catch (Exception e) {
                            log.error("Failed to get info about event {}", eventName, e);
                        }
                        return info;
                    }
            );
            if (event == null) {
                return SeatResponse.error(eventName, "Event  not found");
            }

            SeatResponse response = theatreClient.seats(eventName);
            if (response.errorMessage() != null) {
                return SeatResponse.error(eventName, "Failed to get seats for event.");
            }

            return response;

        } catch (Exception e) {
            log.error("Failed to get seats from event {}", eventName, e);
        }
        return SeatResponse.error(eventName, "Failed to get seats for event. Unknown error.");
    }
}
