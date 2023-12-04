package ru.nspk.performance.transactionshandler.validator;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.nspk.performance.api.TicketRequest;
import ru.nspk.performance.base.TicketInfo;
import ru.nspk.performance.keyvaluestorage.KeyValueStorage;
import ru.nspk.performance.keyvaluestorage.model.Person;
import ru.nspk.performance.transactionshandler.service.TransactionalEventService;
import ru.nspk.performance.transactionshandler.theatreclient.TheatreClient;
import ru.nspk.performance.users.User;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@RequiredArgsConstructor
@Slf4j
public class TicketRequestInputValidator implements ModelValidator<TicketRequest> {

    private final KeyValueStorage keyValueStorage;
    private final TheatreClient theatreClient;

    @Override
    public void validateModel(@NonNull TicketRequest model) throws InterruptedException {
        deduplicate(model.getRequestId());
        checkUserExistsAndEnable(model.getTicketInfoList());
        //checkEvent(model.getEventName()); todo не реализовано пока
        checkEventDate(model.getEventDate());
    }

    private void checkEventDate(String eventDate) {
        DateFormat dateTime = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Instant now = Instant.now();
            if (dateTime.parse(eventDate).before(Date.from(now))) {
                throw new ValidationException("Event expired  " + eventDate + " to current date " + now,
                        this.getClass().getName(),
                        ValidationError.EVENT_DATE_EXPIRED);
            }
        } catch (ParseException e) {
            log.error("Failed to parse event date {}", eventDate, e);
            throw new ValidationException("Wrong date format " + eventDate,
                    this.getClass().getName(),
                    ValidationError.WRONG_EVENT_DATE_FORMAT);
        }
    }

    private void checkEvent(String eventName) throws InterruptedException {
        try {
            String event = Optional.ofNullable(keyValueStorage.<String, String>get(TransactionalEventService.EVENTS_MAP, eventName))
                    .orElse(
                            lookingInTheatre(eventName)
                    );
            keyValueStorage.put(TransactionalEventService.EVENTS_MAP, eventName, event, s -> log.info("Successfully insert new event " + s + " to in-memory."));
        } catch (JsonProcessingException |
                 UnsupportedEncodingException |
                 ExecutionException |
                 TimeoutException e) {
            log.error("Event check event {} in in-memory.", eventName, e);
        }
    }

    private String lookingInTheatre(String eventName) throws ExecutionException, InterruptedException, TimeoutException {
        return theatreClient.events().stream().filter(s -> String.valueOf(eventName).equals(s)).findFirst().orElseThrow(
                () -> new ValidationException(
                        "Event " + eventName + " not found.",
                        this.getClass().getName(),
                        ValidationError.EVENT_NOT_FOUND)
        );
    }

    private void checkUserExistsAndEnable(List<TicketInfo> ticketInfoList) throws InterruptedException {
        for (TicketInfo ticketInfo: ticketInfoList) {
            try {
                Person person = keyValueStorage.<String, Person>get(TransactionalEventService.PERSON_DATA_MAP, ticketInfo.getUserUUID());
                if (person == null) {
                    throw new ValidationException(
                            "Person " + ticketInfo.getUserUUID() + " not found. Need to register",
                            this.getClass().getName(),
                            ValidationError.USER_NOT_FOUND);
                }
            } catch (ExecutionException | TimeoutException e) {
                log.error("Failed to check user {} in in-memory.", ticketInfo.getUserUUID(), e);
            }
        }
    }

    private void deduplicate(long requestId) throws InterruptedException {
        try {
            Long transactionId = keyValueStorage.<Long, Long>get(TransactionalEventService.REQUESTS_MAP, requestId);
            if (transactionId != null && transactionId == Long.MIN_VALUE) {
                throw new ValidationException(
                        "Duplicate of ticket request with id " + requestId + ". Stop handling request.",
                        this.getClass().getName(),
                        ValidationError.DUPLICATE);
            }
        } catch (ExecutionException | TimeoutException e) {
            log.error("Failed to check deduplicate request with id {}", requestId, e);
        }
    }
}
