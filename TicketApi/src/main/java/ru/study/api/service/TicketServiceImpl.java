package ru.study.api.service;

import com.google.protobuf.Timestamp;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.stereotype.Service;
import ru.nspk.performance.api.TicketRequest;
import ru.nspk.performance.base.TicketInfo;
import ru.study.api.dto.TicketCheckResponse;
import ru.study.api.dto.TicketPurchaseResponse;
import ru.study.api.dto.SeatDto;
import ru.study.api.dto.TicketPurchaseRequest;
import ru.study.api.keyvalueclient.TicketStatusClient;
import ru.study.api.model.CorrelationPair;
import ru.study.api.model.Event;
import ru.study.api.model.TicketStatus;
import ru.study.api.producer.QueueProducer;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketServiceImpl implements TicketService {

    private final long saveToKafkaTimeoutMs = 1000;
    private final RequestSequenceService requestSequenceService;
    private final UserService userService;
    private final QueueProducer ticketQueueProducer;
    private final TicketStatusClient ticketStatusClient;
    private final EventService eventService;

    @Override
    public TicketPurchaseResponse createNewTicket(TicketPurchaseRequest request) {
        Instant start = Instant.now();
        Event event;
        if ((event = eventService.getEventByName(request.eventName())) == null) {
            return TicketPurchaseResponse.error("Event not found");
        }
        try {
            String eventDate = new SimpleDateFormat("yyyy-MM-dd").format(event.getDate());
            CorrelationPair correlationPair = requestSequenceService.nextCorrelationPair();
            TicketRequest.Builder builder = TicketRequest.newBuilder()
                    .setRequestId(correlationPair.id())
                    .setEventName(event.getName())
                    .setEventDate(eventDate)
                    .setStart(Timestamp.newBuilder().setSeconds(start.getEpochSecond()));


            request.tickets().stream().map(ticketDto -> {
                String userId = null;
                try {
                    userId = userService.userIdOfPerson(ticketDto.person());
                } catch (Exception e) {
                    log.error("Failed to get userId for person {}", ticketDto.person(), e);
                    throw new RuntimeException("Failed to get userId for person " + ticketDto.person());
                }
                return transformSeatDtoToTicketInfo(ticketDto.seat(), userId);
            }).forEach(builder::addTicketInfo);

            save(builder.build());

            return TicketPurchaseResponse.success(correlationPair.correlationId());
        } catch (Exception e) {
            log.error("Failed to create new purchase ticket request. Get error on request {}.", request, e);
            return TicketPurchaseResponse.error("Failed to generate request check your income parameters");
        }
    }

    @Override
    public TicketCheckResponse checkTicket(String uid) {
        long requestId = -1;
        try {
            requestId = requestSequenceService.requestIdByCorrelationUid(uid);
            TicketStatus ticketStatus = ticketStatusClient.checkTicketStatus(requestId);
            return TicketCheckResponse.of(uid, ticketStatus);
        } catch (NullPointerException e) {
            log.error("Get exception on check ticket with uid {} ", uid, e);
            return TicketCheckResponse.uidNotFound(uid);
        } catch (Exception e) {
            log.error("Failed to check ticket with uid {} and requestId {}", uid, requestId, e);
            return TicketCheckResponse.error(uid);
        }
    }

    private void save(TicketRequest ticketRequest) throws ExecutionException, InterruptedException, TimeoutException {
        ticketQueueProducer
                .send(ticketRequest)
//                .addCallback() добавить сохранение в БД
                .get(saveToKafkaTimeoutMs, TimeUnit.MILLISECONDS);
    }

    private TicketInfo transformSeatDtoToTicketInfo(SeatDto seatDto, String userId) {
        return TicketInfo.newBuilder()
                .setPlace(seatDto.place())
                .setUserUUID(userId)
                .setPrice((float) seatDto.price())
                .build();
    }
}
