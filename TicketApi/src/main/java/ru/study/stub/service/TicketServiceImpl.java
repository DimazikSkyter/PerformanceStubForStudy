package ru.study.stub.service;

import com.google.protobuf.Timestamp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.stereotype.Service;
import ru.nspk.performance.api.TicketRequest;
import ru.nspk.performance.base.TicketInfo;
import ru.study.stub.dto.SeatDto;
import ru.study.stub.dto.TicketPurchaseRequest;
import ru.study.stub.dto.TicketPurchaseResponse;
import ru.study.stub.entity.TicketStatus;
import ru.study.stub.model.CorrelationPair;
import ru.study.stub.producer.QueueProducer;

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

    @Override
    public TicketPurchaseResponse createNewTicket(TicketPurchaseRequest request) {
        Instant start = Instant.now();
        try {
            String eventDate = new DateFormatter().print(request.event().eventDate(), Locale.ENGLISH);
            CorrelationPair correlationPair = requestSequenceService.nextCorrelationPair();
            TicketRequest ticketRequest = TicketRequest.newBuilder()
                    .setRequestId(correlationPair.id())
                    .setEventName(request.event().eventName())
                    .setEventDate(eventDate)
                    .setStart(Timestamp.newBuilder().setSeconds(start.getEpochSecond()))
                    .build();

            request.tickets().stream().map(ticketDto -> {
                String userId = userService.userIdOfPerson(ticketDto.person());
                return transformSeatDtoToTicketInfo(ticketDto.seat(), userId);
            }).forEach(ticketInfo -> ticketRequest.getTicketInfoList().add(ticketInfo));

            save(ticketRequest);

            return TicketPurchaseResponse.success(correlationPair.correlationId());
        } catch (Exception e) {
            return TicketPurchaseResponse.error("Failed to generate request");
        }
    }

    @Override
    public TicketStatus checkTicket(String uid) {
        return null;
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
