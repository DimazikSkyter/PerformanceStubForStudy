package ru.nspk.performance.transactionshandler.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import ru.nspk.performance.model.ReserveDto;

public class ReserveKafkaProducerImpl<V> extends KafkaProducer<ReserveDto> {

    public ReserveKafkaProducerImpl(KafkaTemplate<String, ReserveDto> template) {
        super(template);
    }

    @Override
    public ListenableFuture<SendResult<String, ReserveDto>> sendEvent(String key, ReserveDto value) {
        return super.sendEvent(key, value);
    }
}
