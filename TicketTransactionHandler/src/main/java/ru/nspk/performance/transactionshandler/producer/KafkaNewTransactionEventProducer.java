package ru.nspk.performance.transactionshandler.producer;


import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import ru.nspk.performance.model.NewTransactionEvent;


public class KafkaNewTransactionEventProducer extends KafkaProducer<NewTransactionEvent> {

    public KafkaNewTransactionEventProducer(KafkaTemplate<String, NewTransactionEvent> template) {
        super(template);
    }

    @Override
    public ListenableFuture<SendResult<String, NewTransactionEvent>> sendEvent(String key, NewTransactionEvent event) {
        return super.sendEvent(key, event);
    }
}
