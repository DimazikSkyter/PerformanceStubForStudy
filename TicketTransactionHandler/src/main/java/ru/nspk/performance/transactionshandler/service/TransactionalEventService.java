package ru.nspk.performance.transactionshandler.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hazelcast.jet.datamodel.Tuple2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nspk.performance.api.TicketRequest;
import ru.nspk.performance.events.CreateReserveAction;
import ru.nspk.performance.events.PaymentAction;
import ru.nspk.performance.events.ReserveResponseAction;
import ru.nspk.performance.transactionshandler.keyvaluestorage.KeyValueStorage;
import ru.nspk.performance.transactionshandler.model.PaymentCheckResponse;
import ru.nspk.performance.transactionshandler.model.PaymentRequest;
import ru.nspk.performance.transactionshandler.model.ReversePaymentResponse;
import ru.nspk.performance.transactionshandler.producer.KafkaProducer;
import ru.nspk.performance.transactionshandler.properties.TransactionProperties;
import ru.nspk.performance.transactionshandler.state.TicketTransactionState;
import ru.nspk.performance.transactionshandler.state.TransactionState;
import ru.nspk.performance.transactionshandler.theatreclient.TheatreClient;
import ru.nspk.performance.transactionshandler.timeoutprocessor.TimeoutProcessor;
import ru.nspk.performance.transactionshandler.transformer.TransformException;
import ru.nspk.performance.transactionshandler.transformer.TransformerMultiton;
import ru.nspk.performance.transactionshandler.validator.ValidationException;
import ru.nspk.performance.transactionshandler.validator.ValidatorMultiton;

import java.io.UnsupportedEncodingException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@RequiredArgsConstructor
@Service
@Slf4j
public class TransactionalEventService {

    //todo вынести список мап в корень
    public static final String TRANSACTIONS_MAP = "transactions";
    public static final String USERS_MAP = "users";
    public static final String REQUESTS_MAP = "requests";
    public static final String EVENTS_MAP = "events";


    private final KafkaProducer kafkaProducer;
    private final ValidatorMultiton validators;
    private final TheatreClient theatreClient;
    private final TransformerMultiton transformers;
    private final KeyValueStorage keyValueStorage;
    private final TimeoutProcessor timeoutProcessor;
    private final TransactionProperties transactionProperties;
    private final PaymentService paymentService;

    @Transactional
    public void newTicketEvent(TicketRequest ticketRequest) {
        try {
            Instant start = Instant.now();
            validators.validateModel(ticketRequest);
            TicketTransactionState ticketTransactionState = transformers.transform(ticketRequest);
            ticketTransactionState.setStart(start);

            timeoutProcessor.executeWithTimeout(
                    () -> {
                        try {
                            kafkaProducer.sendTransactionState(ticketTransactionState);
                            keyValueStorage.put(TRANSACTIONS_MAP,
                                    ticketTransactionState.getTransactionId(),
                                    ticketTransactionState,
                                    bytes -> makeReserve(ticketTransactionState));
                        } catch (UnsupportedEncodingException | JsonProcessingException e) {
                            log.error("Catch exception while save transaction {} into IMDG.", ticketTransactionState.getTransactionId());
                        }
                    },
                    Duration.of(transactionProperties.getFullTimeoutDurationMs() - Duration.between(start, Instant.now()).toMillis(), ChronoUnit.MILLIS),
                    () -> rejectTransaction(ticketTransactionState.getTransactionId(), "Timeout exception"));
        } catch (ValidationException validationException) {
            log.error("Get exception on validation ticket {} on new event.", ticketRequest.getEventName(), validationException);
        } catch (TransformException transformException) {
            log.error("Failed to transform transaction {}", ticketRequest.getEventName(), transformException);
        } catch (Exception e) {
            log.error("Get unknown error in new transaction event {}", ticketRequest.getRequestId());
        }
    }

    public void handleReserveResponse(String reserveResponseBody) {
        try {
            validators.validateInput(reserveResponseBody, ReserveResponseAction.class);
            ReserveResponseAction reserveResponseEvent = transformers.transform(reserveResponseBody);
            Long transactionId = keyValueStorage.<String, Long>get(REQUESTS_MAP, reserveResponseEvent.getRequestId());
            if (transactionId == null) {
                log.error("Request of reserve not found {} ", reserveResponseEvent.getRequestId());
                return;
            }
            if (reserveResponseEvent.getReserveId() == 0) {
                rejectTransaction(transactionId, "Reserve not created");
            }
            createPaymentWithTimeout(reserveResponseEvent, transactionId);
        } catch (ValidationException e) {
            log.error("Catch error while validate reserve response in body {}", reserveResponseBody, e);
        } catch (Exception e) {
            log.error("Failed to handle reserve response.", e);
        }
    }

    private void createPaymentWithTimeout(ReserveResponseAction reserveResponseEvent, long transactionId) {
        timeoutProcessor.executeWithTimeout(
                () -> makePayment(reserveResponseEvent, transactionId),
                calculateTimeout(reserveResponseEvent.finishTime(),
                        Duration.of(transactionProperties.getMaxPaymentTimeMs(), ChronoUnit.MILLIS),
                        transactionId),
                () -> checkStatusAndReject(transactionId));
    }

    private void checkStatusAndReject(long transactionId) {
        try {
            TicketTransactionState ticketTransactionState = keyValueStorage.<Long, TicketTransactionState>get(TRANSACTIONS_MAP, transactionId);
            if (!TransactionState.WAIT_FOR_PAYMENT.isEarliestThan(ticketTransactionState.getCurrentState())) {
                rejectTransaction(transactionId, "Timeout while waiting to pay transaction.");
            }
        } catch (Exception e) {
            log.error("Failed while check status and reject transaction {}", transactionId, e);
        }
    }

    private void makePayment(ReserveResponseAction reserveResponseEvent, long transactionId) {
        try {
            PaymentRequest paymentRequest = transformers.transform(reserveResponseEvent);
            paymentService.createPaymentLink(paymentRequest).orTimeout(transactionProperties.getPaymentLinkTimeoutMs(), TimeUnit.MILLISECONDS).thenAccept(paymentLinkBytes -> {
                try {
                    kafkaProducer.sendPaymentLink(paymentLinkBytes).exceptionally(throwable -> {
                        log.error("");
                        rejectTransaction(transactionId, "Failed to store payment link");
                        return null;
                    }).thenAccept(result -> changeStatusToWaitingForPayment(paymentRequest));
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
        keyValueStorage.updateWithCondition(TRANSACTIONS_MAP, paymentRequest.transactionId(), (TicketTransactionState currentTransactionState) -> currentTransactionState, ticketTransactionState -> TransactionState.WAIT_FOR_PAYMENT.equals(ticketTransactionState.getCurrentState()));
    }

    public void handlePaymentResponse(String request) {

        Long transactionId = null;
        try {

            validators.validateInput(request, PaymentCheckResponse.class);
            PaymentCheckResponse paymentCheckResponse = transformers.<String, PaymentCheckResponse>transform(request);

            transactionId = (Long) keyValueStorage.get(REQUESTS_MAP, paymentCheckResponse.getRequestId());
            if (transactionId == null) {
                log.error("Request of payment not found {}.", paymentCheckResponse.getRequestId());
                return;
            }


            if (paymentCheckResponse.isPaymentStatus()) {
                PaymentAction paymentEvent = new PaymentAction();
                Tuple2<Boolean, TicketTransactionState> updateResult = keyValueStorage.<Long, TicketTransactionState>updateWithCondition(TRANSACTIONS_MAP, transactionId, currentTicketTransactionState -> {
                    currentTicketTransactionState.moveOnNextStep(TransactionState.WAIT_FOR_PAYMENT);
                    currentTicketTransactionState.getActions().put("Payment", paymentEvent);
                    return currentTicketTransactionState;
                }, currentTicketTransactionState -> currentTicketTransactionState != null && currentTicketTransactionState.getCurrentState().equals(TransactionState.WAIT_FOR_PAYMENT));

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
            Tuple2<Boolean, TicketTransactionState> transactionStatusTuple = keyValueStorage.<Long, TicketTransactionState>updateWithCondition(TRANSACTIONS_MAP, transactionId, ticketTransactionState -> {
                ticketTransactionState.setCurrentState(TransactionState.REJECT);
                ticketTransactionState.setErrorReason(reason);
                return ticketTransactionState;
            }, ticketTransactionState -> !TransactionState.REJECT.equals(ticketTransactionState.getCurrentState()));
            if (transactionStatusTuple.getKey()) {
                kafkaProducer.sendTransactionState(transactionStatusTuple.getValue());
            }
        } catch (Exception e) {
            log.error("Failed to reject transaction {}.", transactionId, e);
        }
    }

    public void makeReserve(TicketTransactionState ticketTransactionState) {
        CreateReserveAction createReserveEvent = new CreateReserveAction(ticketTransactionState.getEvent().getSeats());
        String requestId = UUID.randomUUID().toString();
        try {
            keyValueStorage.put(REQUESTS_MAP, requestId, ticketTransactionState.getTransactionId(),
                    bytes -> makeReserveToTheatre(requestId, ticketTransactionState, createReserveEvent));
        } catch (Exception e) {
            log.error("Catch exception while try to save requestId {} for transaction {}\n",
                    requestId,
                    ticketTransactionState.getTransactionId(),
                    e);
        }
    }

    private void makeReserveToTheatre(String requestId, TicketTransactionState transactionState, CreateReserveAction createReserveEvent) {
        try {
            theatreClient.reserve(requestId, createReserveEvent.getEventId(), createReserveEvent.getSeats(), this::handleReserveResponse);
            transactionState.getActions().put("create reserve", createReserveEvent);
            transactionState.moveOnNextStep(TransactionState.NEW_TRANSACTION);
            kafkaProducer.sendEvent(transactionState.getTransactionId(), createReserveEvent.getBytes());
            keyValueStorage.<Long, TicketTransactionState>updateWithCondition(TRANSACTIONS_MAP,
                    transactionState.getTransactionId(),
                    ts -> transactionState,
                    inMemoryTransactionState -> inMemoryTransactionState.getCurrentState().isEarliestThan(transactionState.getCurrentState()));
        } catch (TimeoutException e) {
            log.error("Failed to get reserve response for transaction {} during wait time", transactionState.getTransactionId());
            //todo реджекта нет, потому что клиент потом вынесется в отдельный модуль и в этом отпадет смысл
        } catch (Exception e) {
            log.error("Failed to generate reserve to transaction {}", transactionState.getTransactionId(), e);
            rejectTransaction(transactionState.getTransactionId(), "reserve in theatre exception");
        }
    }

    private Duration calculateTimeout(Instant merchantPaymentMaxTime, Duration maxTimeout, long transactionId) {
        Duration merchantTimeout = Duration.between(Instant.now(), merchantPaymentMaxTime);
        if (merchantTimeout.isNegative()) {
            throw new RuntimeException("Transaction " + transactionId + " time has passed.");
        }
        return merchantTimeout.compareTo(maxTimeout) > 0 ? maxTimeout : merchantTimeout;
    }
}
