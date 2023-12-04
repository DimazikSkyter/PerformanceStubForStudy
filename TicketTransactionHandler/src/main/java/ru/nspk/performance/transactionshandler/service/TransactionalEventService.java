package ru.nspk.performance.transactionshandler.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.protobuf.ByteString;
import com.google.protobuf.Timestamp;
import com.hazelcast.jet.datamodel.Tuple2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nspk.performance.action.*;
import ru.nspk.performance.api.PaymentLinkToApi;
import ru.nspk.performance.api.TicketRequest;
import ru.nspk.performance.keyvaluestorage.KeyValueStorage;
import ru.nspk.performance.transactionshandler.dto.PaymentLinkResponse;
import ru.nspk.performance.transactionshandler.dto.PaymentOrderResult;
import ru.nspk.performance.transactionshandler.model.Seat;
import ru.nspk.performance.transactionshandler.payment.PaymentClient;
import ru.nspk.performance.transactionshandler.producer.KafkaProducer;
import ru.nspk.performance.transactionshandler.properties.InMemoryProperties;
import ru.nspk.performance.transactionshandler.properties.TransactionProperties;
import ru.nspk.performance.transactionshandler.state.TicketTransactionState;
import ru.nspk.performance.transactionshandler.state.TransactionState;
import ru.nspk.performance.transactionshandler.theatreclient.TheatreClient;
import ru.nspk.performance.transactionshandler.theatreclient.dto.ReserveResponse;
import ru.nspk.performance.transactionshandler.timeoutprocessor.TimeoutProcessor;
import ru.nspk.performance.transactionshandler.transformer.TransformException;
import ru.nspk.performance.transactionshandler.transformer.TransformerMultiton;
import ru.nspk.performance.transactionshandler.validator.ValidationException;
import ru.nspk.performance.transactionshandler.validator.ValidatorMultiton;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;


//todo очередность событий может быть нарушена
@RequiredArgsConstructor
@Service
@Slf4j
public class TransactionalEventService {

    //todo вынести список мап в корень
    public static final String TRANSACTIONS_MAP = "transactions";
    public static final String PERSON_DATA_MAP = "person_data";
    public static final String REQUESTS_MAP = "requests";
    public static final String EVENTS_MAP = "events";
    private final KafkaProducer kafkaProducer;
    private final ValidatorMultiton validators;
    private final TheatreClient theatreClient;
    private final PaymentClient paymentClient;
    private final TransformerMultiton transformers;
    private final KeyValueStorage keyValueStorage;
    private final TimeoutProcessor timeoutProcessor;
    private final TransactionProperties transactionProperties;
    private final InMemoryProperties inMemoryProperties;
    private AtomicLong requestIterator = new AtomicLong(0);

    @Transactional
    public void newTicketEvent(TicketRequest ticketRequest) {
        long transactionId = -1;
        log.info("Create new ticket request\n{}", ticketRequest);
        try {
            Instant start = Instant.now();
            validators.validateModel(ticketRequest);
            TicketTransactionState ticketTransactionState = transformers.transform(ticketRequest);
            ticketTransactionState.setStart(start);

            timeoutProcessor.executeWithTimeout(
                    Duration.of(transactionProperties.getFullTimeoutDurationMs() - Duration.between(start, Instant.now()).toMillis(), ChronoUnit.MILLIS),
                    () -> rejectTransaction(ticketTransactionState.getTransactionId(), "Timeout exception"));

            transactionId = ticketTransactionState.getTransactionId();
            kafkaProducer.sendTransactionState(transactionId, ticketTransactionState.getBytes());
            keyValueStorage.put(TRANSACTIONS_MAP,
                    transactionId,
                    ticketTransactionState.getBytes(),
                    bytes -> makeReserve(ticketTransactionState));
        } catch (UnsupportedEncodingException | JsonProcessingException e) {
            log.error("Catch exception while save transaction {} into IMDG.", transactionId);
        } catch (ValidationException validationException) {
            log.error("Get exception on validation ticket {} on new event.", ticketRequest.getEventName(), validationException);
        } catch (TransformException transformException) {
            log.error("Failed to transform transaction {}", ticketRequest.getEventName(), transformException);
        } catch (Exception e) {
            log.error("Get unknown error in new transaction event {}", ticketRequest.getRequestId(), e);
        }
    }


    public void makeReserve(TicketTransactionState ticketTransactionState) {
        log.debug("Create new reserve for ticket transaction state {}", ticketTransactionState);
        CreateReserveAction createReserveEvent = CreateReserveAction.builder()
                .seats((ticketTransactionState.getEvent().getSeats().stream().map(Seat::place).reduce((s, s2) -> s + "," + s2).orElseThrow(RuntimeException::new)))
                .build();
        String requestId = getRandomRequestId();
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

    private void makeReserveToTheatre(String requestId, TicketTransactionState ticketTransactionState, CreateReserveAction createReserveEvent) {
        log.debug("Make reserve to theatre with request id {}", requestId);
        try {
            theatreClient.reserve(requestId, ticketTransactionState.getEvent().getEventName(), createReserveEvent.getSeats(), this::handleReserveResponse);
            ticketTransactionState.getActions().put("create reserve", createReserveEvent);
            ticketTransactionState.moveOnNextStep(TransactionState.NEW_TRANSACTION);
            kafkaProducer.sendAction(ticketTransactionState.getTransactionId(), createReserveEvent.getBytes());
            updateTransactionState(ticketTransactionState);
        } catch (Exception e) {
            log.error("Failed to generate reserve to transaction {}", ticketTransactionState.getTransactionId(), e);
            rejectTransaction(ticketTransactionState.getTransactionId(), "reserve in theatre exception");
        }
    }

    public void handleReserveResponse(String reserveResponseBody) {
        log.debug("New income reserve response body {}", reserveResponseBody);
        try {
            validators.validateInput(reserveResponseBody, ReserveResponseAction.class);
            ReserveResponseAction reserveResponseEvent = transformers.<ReserveResponseAction>transform(reserveResponseBody, ReserveResponse.class);
            Long transactionId = keyValueStorage.<String, Long>get(REQUESTS_MAP, reserveResponseEvent.getRequestId());
            if (transactionId == null) {
                log.error("Request of handle reserve not found {} ", reserveResponseEvent.getRequestId());
                return;
            }
            if (reserveResponseEvent.getReserveId() == 0) {
                rejectTransaction(transactionId, "Reserve not created");
                return;
            }
            keyValueStorage.<Long, byte[]>getAsync(TRANSACTIONS_MAP, transactionId)
                    .toCompletableFuture()
                    .thenAccept(transactionStateBytes -> updateTransactionStateAfterSuccessfulReserve(reserveResponseEvent, transactionId, transactionStateBytes))
                    .orTimeout(inMemoryProperties.getTimeout(), TimeUnit.MILLISECONDS)
                    .exceptionally(throwable -> {
                        log.error("Failed to get tranasction from key  value with exception", throwable);
                        rejectTransaction(transactionId, "Failed to get transaction by state");
                        return null;
                    });
        } catch (ValidationException e) {
            log.error("Catch error while validate reserve response in body {}", reserveResponseBody, e);
        } catch (Exception e) {
            log.error("Failed to handle reserve response.", e);
        }
    }

    private void updateTransactionStateAfterSuccessfulReserve(ReserveResponseAction reserveResponseEvent, long transactionId, byte[] transactionState) {
        log.debug("Update transaction state with transactionId {} after successful reserve {}", transactionId, reserveResponseEvent);
        try {
            TicketTransactionState ticketTransactionState = TicketTransactionState.from(transactionState);
            ticketTransactionState.getActions().put("reserved", reserveResponseEvent);
            ticketTransactionState.moveOnNextStep(TransactionState.RESERVE_REQUEST);
            updateTransactionState(ticketTransactionState);
            createPaymentLinkToPerson(reserveResponseEvent, ticketTransactionState);
        } catch (Exception e) {
            log.error("Failed to update transaction {} after successful reserve", transactionId, e);
            rejectTransaction(transactionId, "Failed to update transaction after successful reserve");
        }
    }

    private void createPaymentLinkToPerson(ReserveResponseAction reserveResponseEvent, TicketTransactionState ticketTransactionState) {
        log.debug("Create payment link to person for transactionId {}", ticketTransactionState.getTransactionId());
        try {
            PaymentLinkAction paymentLinkAction = transformers.<ReserveResponseAction, PaymentLinkAction>transform(reserveResponseEvent);
            paymentLinkAction.setAccount("temporary_mock");
            paymentLinkAction.setPurpose(buildPurpose(ticketTransactionState, paymentLinkAction.getAmount()));
            ticketTransactionState.getActions().put("create_payment_link", paymentLinkAction);
            ticketTransactionState.moveOnNextStep(TransactionState.RESERVED);
            keyValueStorage.put(REQUESTS_MAP, paymentLinkAction.getRequestId(), ticketTransactionState.getTransactionId(),
                    bytes -> paymentClient.createPaymentLinkInPaymentService(paymentLinkAction, this::handlePaymentLinkResponse));
            saveEventToKafkaAndUpdateTransactionState(ticketTransactionState, paymentLinkAction);
        } catch (Exception e) {
            log.error("Get exception while make payment for transactionId {}", ticketTransactionState.getTransactionId(), e);
            rejectTransaction(ticketTransactionState.getTransactionId(), "Something wrong while make payment");
        }
    }

    private void handlePaymentLinkResponse(String paymentLinkBody) {
        log.debug("New income payment link body {}", paymentLinkBody);
        try {
            validators.validateInput(paymentLinkBody, PaymentLinkResponse.class);
            PaymentLinkResponseAction paymentLinkResponseAction = transformers.<PaymentLinkResponseAction>transform(paymentLinkBody, PaymentLinkResponse.class);
            Long transactionId = keyValueStorage.<String, Long>get(REQUESTS_MAP, paymentLinkResponseAction.getRequestId());
            if (transactionId == null) {
                log.error("Request of handle payment not found {} ", paymentLinkResponseAction.getRequestId());
                return;
            }
            if (paymentLinkResponseAction.getPaymentLinkBytes() == null) {
                //todo сделать ретрай?
                rejectTransaction(transactionId, "Failed to create payment link.");
                return;
            }
            keyValueStorage.<Long, byte[]>getAsync(TRANSACTIONS_MAP, transactionId)
                    .toCompletableFuture()
                    .thenAccept(transactionStateBytes -> updateTransactionStateAfterSuccessfulPaymentLinkResponse(paymentLinkResponseAction, transactionId, transactionStateBytes))
                    .orTimeout(inMemoryProperties.getTimeout(), TimeUnit.MILLISECONDS)
                    .exceptionally(throwable -> rejectTransaction(transactionId, "Failed to get transaction by state"));
        } catch (ValidationException e) {
            log.error("Catch error while validate payment link response in body {}", paymentLinkBody, e);
        } catch (Exception e) {
            log.error("Failed to handle reserve response.", e);
        }
    }

    private void updateTransactionStateAfterSuccessfulPaymentLinkResponse(PaymentLinkResponseAction paymentLinkResponseAction, long transactionId, byte[] transactionState) {
        log.debug("Update transaction state after successful payment link response for transactionId {}", transactionId);
        try {
            TicketTransactionState ticketTransactionState = TicketTransactionState.from(transactionState);
            ticketTransactionState.getActions().put("payment_link_created", paymentLinkResponseAction);
            ticketTransactionState.moveOnNextStep(TransactionState.WAIT_FOR_PAYMENT_LINK);
            updateTransactionState(ticketTransactionState);
            sendPaymentLinkToKafkaToApi(paymentLinkResponseAction, ticketTransactionState);
        } catch (Exception e) {
            log.error("Failed to update transaction {} after successful reserve", transactionId, e);
            rejectTransaction(transactionId, "Failed to update transaction after successful reserve");
        }
    }

    private void sendPaymentLinkToKafkaToApi(PaymentLinkResponseAction paymentLinkResponseAction, TicketTransactionState ticketTransactionState) throws JsonProcessingException {
        Instant paymentLinkToApiStartTime = Instant.now();
        log.debug("Send payment link to kafka to api {} for transactionId {} at {}", paymentLinkResponseAction, ticketTransactionState.getTransactionId(), paymentLinkToApiStartTime);
        long requestId = requestIterator.get();
        PaymentLinkToApi paymentLinkToApi = PaymentLinkToApi.newBuilder()
                .setRequestId(requestId)
                .setLinkedRequestId(ticketTransactionState.getTransactionId())
                .setPaymentLink(ByteString.copyFrom(paymentLinkResponseAction.getPaymentLinkBytes()))
                .setPaymentLinkTimeToPayMs(paymentLinkResponseAction.getTimeToPayMs())
                .setPaymentLinkCreatedTime(Timestamp.newBuilder()
                        .setSeconds(paymentLinkResponseAction.getPaymentLinkCreated().getEpochSecond())
                        .setNanos(paymentLinkResponseAction.getPaymentLinkCreated().getNano())
                        .build())
                .build();
        kafkaProducer.sendPaymentLinkForApi(paymentLinkToApi.toByteArray());
        ticketTransactionState.moveOnNextStep(TransactionState.PAYMENT_LINK_CREATED);
        SendPaymentLinkToApiAction sendPaymentLinkToApiAction = SendPaymentLinkToApiAction.builder()
                .createdTime(paymentLinkToApiStartTime)
                .transactionId(ticketTransactionState.getTransactionId())
                .requestId(requestId)
                .build();
        ticketTransactionState.getActions().put("send_payment_link_to_api", sendPaymentLinkToApiAction);
        saveEventToKafkaAndUpdateTransactionState(ticketTransactionState, sendPaymentLinkToApiAction);
    }

    public void handlePaymentResult(String paymentResult) {
        log.debug("Get payment result {}", paymentResult);
        try {
            validators.validateInput(paymentResult, PaymentOrderResult.class);
            PaymentOrderResult paymentOrderResult = transformers.<PaymentOrderResult>transform(paymentResult, PaymentOrderResult.class);
            Long transactionId = (Long) keyValueStorage.get(REQUESTS_MAP, paymentOrderResult.getRequestId());
            if (transactionId == null) {
                log.error("Request of payment not found {}.", paymentOrderResult.getRequestId());
                return;
            }
            keyValueStorage.<Long, byte[]>getAsync(TRANSACTIONS_MAP, transactionId)
                    .toCompletableFuture()
                    .thenAccept(transactionStateBytes -> updateTransactionStateAfterPaymentResult(paymentOrderResult, transactionId, transactionStateBytes))
                    .orTimeout(inMemoryProperties.getTimeout(), TimeUnit.MILLISECONDS)
                    .exceptionally(throwable -> rejectTransaction(transactionId, "Failed to get transaction by state"));
        } catch (Exception e) {
            log.error("Get error while handle payment result {}", paymentResult, e);
        }
    }

    public void updateTransactionStateAfterPaymentResult(PaymentOrderResult paymentOrderResult, Long transactionId, byte[] transactionStateBytes) {
        try {
            TicketTransactionState ticketTransactionState = TicketTransactionState.from(transactionStateBytes);
            PaymentResultAction paymentLinkResponseAction = transformers.<PaymentOrderResult, PaymentResultAction>transform(paymentOrderResult, PaymentResultAction.class);
            paymentLinkResponseAction.setTransactionId(transactionId); //todo подумать как перенести в transform
            ticketTransactionState.getActions().put("payment result", paymentLinkResponseAction);

            if (!paymentOrderResult.isSuccess()) {
                ticketTransactionState.rejectTransaction("Unsuccessful payment result");
            } else {
                ticketTransactionState.moveOnNextStep(TransactionState.WAIT_FOR_PAYMENT);
            }

            saveEventToKafkaAndUpdateTransactionState(ticketTransactionState, paymentLinkResponseAction);
        } catch (Exception e) {
            log.error("Failed to update transaction state for id {} after payment result {}", transactionId, paymentOrderResult, e);
            rejectTransaction(transactionId, "Failed to update transaction state after payment result");
        }
    }

    private void saveEventToKafkaAndUpdateTransactionState(TicketTransactionState ticketTransactionState, Action action) throws JsonProcessingException {
        kafkaProducer.sendAction(ticketTransactionState.getTransactionId(), action.getBytes())
                .thenApply(longSendResult -> {
                    log.debug("Successfully save to kafka action {} for transactionId {}", action.getClass().getName(), ticketTransactionState.getTransactionId());
                    return longSendResult;
                })
                .exceptionally(throwable -> {
                    log.error("Failed to save transaction {} to kafka with error", ticketTransactionState.getTransactionId(), throwable);
                    return null;
                })
                .thenAccept(longSendResult -> updateTransactionState(ticketTransactionState))
                .toCompletableFuture()
                .orTimeout(inMemoryProperties.getTimeout(), TimeUnit.MILLISECONDS)
                .exceptionally(throwable -> {
                    log.error("Failed to save transaction {} to key-value with error", ticketTransactionState.getTransactionId(), throwable);
                    return null;
                });
    }

    private void updateTransactionState(TicketTransactionState ticketTransactionState) {
        log.debug("Update transaction state {}", ticketTransactionState);
        keyValueStorage.<Long, byte[]>updateWithCondition(TRANSACTIONS_MAP,
                ticketTransactionState.getTransactionId(),
                bytes -> {
                    try {
                        return ticketTransactionState.getBytes();
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("Failed to update transaction state", e);
                    }
                },
                inMemoryTransactionStateBytes -> {
                    try {
                        TicketTransactionState currentTs = TicketTransactionState.from(inMemoryTransactionStateBytes);
                        boolean earliestThan = currentTs.getCurrentState().isEarliestThan(ticketTransactionState.getCurrentState());
                        log.debug("Condition of update value is {} for transactionId {}", earliestThan, ticketTransactionState.getTransactionId());
                        return earliestThan;
                    } catch (Exception e) {
                        log.error("Condition to update transaction {} is false. Current state {}", ticketTransactionState.getTransactionId(), ticketTransactionState.getCurrentState(), e);
                        return false;
                    }
                }
        );
    }

    public Void rejectTransaction(long transactionId, String reason) { //перевести reason в enum
        try {
            Tuple2<Boolean, byte[]> transactionStatusTuple = keyValueStorage.<Long, byte[]>updateWithCondition(TRANSACTIONS_MAP, transactionId, bytes -> {
                TicketTransactionState ticketTransactionState = null;
                try {
                    ticketTransactionState = TicketTransactionState.from(bytes);
                    ticketTransactionState.setCurrentState(TransactionState.REJECT);
                    ticketTransactionState.setErrorReason(reason);
                    return ticketTransactionState.getBytes();
                } catch (IOException e) {
                    throw new RuntimeException("Failed to reject transaction " + transactionId, e);
                }
            }, bytes -> {
                try {
                    TransactionState currentState = TicketTransactionState.from(bytes).getCurrentState();
                    return ! (TransactionState.REJECT.equals(currentState) || TransactionState.COMPLETE.equals(currentState));
                } catch (IOException e) {
                    return false;
                }
            });
            if (Boolean.TRUE.equals(transactionStatusTuple.getKey())) {
                kafkaProducer.sendTransactionState(transactionId, transactionStatusTuple.getValue());
            }
        } catch (Exception e) {
            log.error("Failed to reject transaction {}.", transactionId, e);
        }
        return null;
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

    private String buildPurpose(TicketTransactionState ticketTransactionState, double amount) {
        return String.format("Purchase for event %s for seats %s for total amount of %s",
                ticketTransactionState
                        .getEvent().getEventName(),
                ticketTransactionState
                        .getEvent()
                        .getSeats().stream()
                        .map(Seat::place)
                        .reduce((s, s2) -> s + "," + s2).orElseThrow(RuntimeException::new),
                amount);
    }

    //todo перенести в трансформер
    private String getRandomRequestId() {
        return UUID.randomUUID().toString();
    }
}
