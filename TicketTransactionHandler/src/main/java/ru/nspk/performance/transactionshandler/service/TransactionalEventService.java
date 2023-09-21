package ru.nspk.performance.transactionshandler.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hazelcast.jet.datamodel.Tuple2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nspk.performance.api.TicketRequest;
import ru.nspk.performance.theatre.model.ReserveResponse;
import ru.nspk.performance.transactionshandler.keyvaluestorage.KeyValueStorage;
import ru.nspk.performance.transactionshandler.model.*;
import ru.nspk.performance.transactionshandler.producer.KafkaProducer;
import ru.nspk.performance.transactionshandler.properties.TransactionProperties;
import ru.nspk.performance.transactionshandler.state.TicketTransactionState;
import ru.nspk.performance.transactionshandler.state.TransactionState;
import ru.nspk.performance.transactionshandler.theatreclient.TheatreClient;
import ru.nspk.performance.transactionshandler.timeoutprocessor.TimeoutProcessor;
import ru.nspk.performance.transactionshandler.transformer.ReserveResponseTransformer;
import ru.nspk.performance.transactionshandler.transformer.TransformException;
import ru.nspk.performance.transactionshandler.transformer.TransformerMultiton;
import ru.nspk.performance.transactionshandler.validator.ValidationException;
import ru.nspk.performance.transactionshandler.validator.Validator;

import java.io.UnsupportedEncodingException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@RequiredArgsConstructor
@Service
@Slf4j
public class TransactionalEventService {

    public static final String TRANSACTION_MAP = "transactions";
    public static final String REQUESTS_MAP = "requests";

    private final KafkaProducer kafkaProducer;
    private final Validator validator;
    private final TheatreClient theatreClient;
    private final ReserveResponseTransformer eventTransformer;
    private final TransformerMultiton transformers;
    private final KeyValueStorage keyValueStorage;
    private final TimeoutProcessor timeoutProcessor;
    private final TransactionProperties transactionProperties;
    private final PaymentService paymentService;

    @Transactional
    public void newTicketEvent(TicketRequest ticketRequest) {
        try {
            Instant start = Instant.now();
            validator.validate(ticketRequest);
            TicketTransactionState ticketTransactionState = transformers.transform(ticketRequest);

            timeoutProcessor.executeWithTimeout(
                    () -> {
                        try {
                            kafkaProducer.sendTransactionState(ticketTransactionState);
                            keyValueStorage.put(TRANSACTION_MAP,
                                    ticketTransactionState.getTransactionId(),
                                    ticketTransactionState,
                                    bytes -> makeReserve(
                                            ticketTransactionState.getTransactionId(),
                                            (CreateReserveEvent) ticketTransactionState.getEvents().get("MakeReserve")
                                    ));
                        } catch (UnsupportedEncodingException | JsonProcessingException e) {
                            log.error("Catch exception while save transaction {} into IMDG.", ticketTransactionState.getTransactionId());
                        }
                    },
                    Duration.of(transactionProperties.getFullTimeoutDurationMs() - Duration.between(start, Instant.now()).toMillis(), ChronoUnit.MILLIS),
                    () -> rejectTransaction(
                            ticketTransactionState.getTransactionId(), "Timeout exception"));
        } catch (ValidationException validationException) {
            log.error("Get exception on validation ticket {} on new event.", ticketRequest.getEventId(), validationException);
        } catch (TransformException transformException) {
            log.error("Failed to transform transaction {}", ticketRequest.getEventId(), transformException);
        }
    }

    public void handleReserveResponse(ReserveResponse reserveResponse) {
        try {
            ReserveResponseEvent reserveResponseEvent = eventTransformer.transform(reserveResponse);
            Long transactionId = (Long) keyValueStorage.get(REQUESTS_MAP, reserveResponse.getRequestId());
            if (transactionId == null) {
                log.error("Request of reserve not found {} ", reserveResponse.getRequestId());
                return;
            }

            if (reserveResponse.getReserveId() <= 0) {
                rejectTransaction(transactionId, "Validation exception");
            }

            validator.validate(reserveResponseEvent);

            createPaymentWithTimeout(reserveResponseEvent, transactionId);
        } catch (Exception e) {

        }
    }

    private void createPaymentWithTimeout(ReserveResponseEvent reserveResponseEvent, long transactionId) {
        timeoutProcessor.executeWithTimeout(
                () -> makePayment(reserveResponseEvent, transactionId),
                calculateTimeout(
                        reserveResponseEvent.finishTime(),
                        Duration.of(transactionProperties.getMaxPaymentTimeMs(), ChronoUnit.MILLIS),
                        transactionId),
                () -> checkStatusAndReject(transactionId)
        );
    }

    private void checkStatusAndReject(long transactionId) {
        try {
            TicketTransactionState ticketTransactionState = keyValueStorage.<Long, TicketTransactionState>get(TRANSACTION_MAP, transactionId);
            if (!TransactionState.WAIT_FOR_PAYMENT.isEarliestThan(ticketTransactionState.getCurrentState())) {
                rejectTransaction(transactionId, "Timeout while waiting to pay transaction.");
            }
        } catch (Exception e) {
            log.error("Failed while check status and reject transaction {}", transactionId, e);
        }
    }

    private void makePayment(ReserveResponseEvent reserveResponseEvent, long transactionId) {
        try {
            PaymentRequest paymentRequest = transformers.transform(reserveResponseEvent);
            paymentService.createPaymentLink(paymentRequest)
                    .thenAccept(httpResponse -> {
                        try {
                            byte[] paymentLinkBytes = httpResponse.body();
                            kafkaProducer.sendPaymentLink(paymentLinkBytes)
                                    .addCallback(result -> changeStatusToWaitingForPayment(paymentRequest),
                                            result -> rejectTransaction(transactionId, "Failed to store payment link"));
                        } catch (Exception e) {
                            log.error("Failed to create payment link for transactionId {}", transactionId, e);
                            rejectTransaction(transactionId, "Something wrong with income payment link response");
                        }
                    });
        } catch (Exception e) {
            log.error("Get exception while make payment for transactionId {}", transactionId, e);
            rejectTransaction(transactionId, "Something wrong while make payment");
        }
    }

    private void changeStatusToWaitingForPayment(PaymentRequest paymentRequest) {
        keyValueStorage.updateWithCondition(
                TRANSACTION_MAP,
                paymentRequest.transactionId(),
                (TicketTransactionState currentTransactionState) -> currentTransactionState,
                ticketTransactionState -> TransactionState.WAIT_FOR_PAYMENT.equals(ticketTransactionState.getCurrentState())
        );
    }

    public void handlePaymentResponse(PaymentCheckResponse paymentCheckResponse) {

        Long transactionId = null;
        try {
            transactionId = (Long) keyValueStorage.get(REQUESTS_MAP, paymentCheckResponse.getRequestId());
            if (transactionId == null) {
                log.error("Request of payment not found {}.", paymentCheckResponse.getRequestId());
                return;
            }
            validator.validate(paymentCheckResponse);

            if (paymentCheckResponse.isPaymentStatusSuccess()) {
                PaymentEvent paymentEvent = new PaymentEvent();
                Tuple2<Boolean, TicketTransactionState> updateResult = keyValueStorage.<Long, TicketTransactionState>updateWithCondition(
                        TRANSACTION_MAP,
                        transactionId,
                        currentTicketTransactionState -> {
                            currentTicketTransactionState.moveOnNextStep(TransactionState.WAIT_FOR_PAYMENT);
                            currentTicketTransactionState.getEvents().put("Payment", paymentEvent);
                            return currentTicketTransactionState;
                        },
                        currentTicketTransactionState -> currentTicketTransactionState != null
                                && currentTicketTransactionState.getCurrentState().equals(TransactionState.WAIT_FOR_PAYMENT)
                );

                if (!updateResult.getKey() && updateResult.getValue() == null) {
                    log.error("Transaction {} in handle payment response method not found.", transactionId);
                } else if (!updateResult.getKey()) {
                    log.error("Transaction {} is not in correct status {}.", transactionId, TransactionState.WAIT_FOR_PAYMENT.name());
                } else {
                    kafkaProducer.sendEvent(transactionId, paymentEvent.getBytes());
                }
            } else {
                rejectTransaction(transactionId, "Payment decline");
            }
        } catch (Exception e) {
            log.error("Catch error while handle payment response for {}", transactionId, e);
        }
    }


    //todo в случае прихода оплаты, а операция находится в отказе или реджекте
    public void handleReversePayment(ReversePaymentResponse reversePaymentResponse) {
        // если положительный переводим транзакцию в reject
        // отбрасываем эвент в кафку


        // создается эвент на ручной разбор отбрасывается в кафку
        // состояние переводится в ручной разбор
    }

    public void rejectTransaction(long transactionId, String reason) { //перевести reason в enum
        try {
            Tuple2<Boolean, TicketTransactionState> transactionStatusTuple = keyValueStorage.<Long, TicketTransactionState>updateWithCondition(
                    TRANSACTION_MAP,
                    transactionId,
                    ticketTransactionState -> {
                        ticketTransactionState.setCurrentState(TransactionState.REJECT);
                        ticketTransactionState.setErrorReason(reason);
                        return ticketTransactionState;
                    },
                    ticketTransactionState -> !TransactionState.REJECT.equals(ticketTransactionState.getCurrentState()));
            if (transactionStatusTuple.getKey()) {
                kafkaProducer.sendTransactionState(transactionStatusTuple.getValue());
            }
        } catch (Exception e) {
            log.error("Failed to reject transaction {}.", transactionId, e);
        }
    }

    public void makeReserve(long transactionId, CreateReserveEvent createReserveEvent) {
        String requestId = UUID.randomUUID().toString();
        try {
            keyValueStorage.put("requests",
                    requestId,
                    transactionId,
                    bytes -> makeReserveToTheatre(requestId, transactionId, createReserveEvent));
        } catch (Exception e) {
            log.error("Catch exception while try to save requestId {} for transaction {}\n",
                    requestId, transactionId, e);
        }
    }

    private void makeReserveToTheatre(String requestId, long transactionId, CreateReserveEvent createReserveEvent) {
        try {
            theatreClient.reserve(
                    requestId,
                    createReserveEvent.getEventId(),
                    createReserveEvent.getSeats(),
                    this::handleReserveResponse);
        } catch (TimeoutException e) {
            log.error("Failed to get reserve response for transaction {} during wait time", transactionId);
            //todo реджекта нет, потому что клиент потом вынесется в отдельный модуль и в этом отпадет смысл
        } catch (Exception e) {
            log.error("Failed to generate reserve to transaction {}", transactionId, e);
            rejectTransaction(transactionId, "reserve in theatre exception");
        }
    }

    private Duration calculateTimeout(Instant merchantPaymentMaxTime, Duration maxTimeout, long transactionId) {
        Duration merchantTimeout = Duration.between(Instant.now(), merchantPaymentMaxTime);
        if (merchantTimeout.isNegative()) {
            throw new RuntimeException("Transaction " + transactionId + " time has passed.");
        }
        return merchantTimeout
                .compareTo(maxTimeout) > 0 ?
                maxTimeout :
                merchantTimeout;
    }
}
