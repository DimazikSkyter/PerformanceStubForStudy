package ru.nspk.performance.transactionshandler.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nspk.performance.api.TicketRequest;
import ru.nspk.performance.transactionshandler.model.PaymentCheckResponse;
import ru.nspk.performance.transactionshandler.model.PaymentSystem;
import ru.nspk.performance.transactionshandler.model.ReserveResponse;
import ru.nspk.performance.transactionshandler.keyvaluestorage.KeyValueStorage;
import ru.nspk.performance.transactionshandler.model.ReversePaymentResponse;
import ru.nspk.performance.transactionshandler.producer.KafkaProducer;
import ru.nspk.performance.transactionshandler.state.TicketTransaction;
import ru.nspk.performance.transactionshandler.transformer.TicketRequestTransformer;
import ru.nspk.performance.transactionshandler.validator.ValidationException;
import ru.nspk.performance.transactionshandler.validator.Validator;

@RequiredArgsConstructor
@Service
@Slf4j
public class TransactionalEventService {

    private final KafkaProducer kafkaProducer;
    private final Validator validator;
    private final TicketRequestTransformer transformer;
    private final KeyValueStorage<Integer, TicketTransaction> keyValueStorage;

    @Transactional
    public void newTicketEvent(TicketRequest ticketRequest) {
        try {
            validator.validate(ticketRequest);
            TicketTransaction ticketTransaction = transformer.transform(ticketRequest);
            kafkaProducer.sendEvent(String.valueOf(ticketRequest.getEventId()), ticketTransaction);
            keyValueStorage.put(ticketRequest.getEventId(), ticketTransaction);
//            makeReserveEvent(eventDto);
        } catch (ValidationException validationException) {
            //отправка акноледжа
//            rejectTransaction(validationException.getMessage());
        } catch (Exception exception) {

        }

        // Создаем запрос в кафку на отправку в театр
        // Сохраняем state в ожидание ответа
    }

    public void handleReserveResponse(ReserveResponse reserveResponse) {
        // если негативный рапартуем в кафку
        // переводим в состояние Reject

        // если позитивный создаем таймер на проведение оплаты за время резерва, рапортуем в кафку
        // переводим в статус в формирование запроса к платежной системе
        // создаем запрос к системе оплаты, кладем в кафку
        // переводим состояние в ожидание ответа от платежной системы
    }

    public void handlePaymentSystemResponse(PaymentSystem paymentSystem) {
        //если негативный, то рапортуем в кафку переводим в Reject
        // говорим о снятии резерва

        // если позитивный, переводим состояние на синхронизацию с театром
        // отбрасываем в кафку
        // формируем запрос на синхронизацию с театром, отбрасываем в кафку
        // переводим в состояние ожидание синхронизации с театром
    }

    public void handlePaymentResponse(PaymentCheckResponse paymentCheckResponse) {
        // если отрицательный
        // переводим откат оплаты, создаем запрос в кафку к paymentsystem на откат оплаты
        // кидаем запрос в кафку переводим в состояние ожидание отката транзакции


        // если положительный, переводим состяние в завершено
        // отбрасываем эвент для базы в кафку
    }

    public void handleReversePayment(ReversePaymentResponse reversePaymentResponse) {
        // если положительный переводим транзакцию в reject
        // отбрасываем эвент в кафку


        // создается эвент на ручной разбор отбрасывается в кафку
        // состояние переводится в ручной разбор
    }
}
