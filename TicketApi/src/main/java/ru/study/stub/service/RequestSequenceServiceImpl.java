package ru.study.stub.service;

import org.springframework.stereotype.Service;
import ru.study.stub.model.CorrelationPair;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class RequestSequenceServiceImpl implements RequestSequenceService {

    private final AtomicLong sequence;
    private final Map<String, Long> correlationMap;


    public RequestSequenceServiceImpl() {
        this(new AtomicLong(0), new HashMap<>());
    }

    public RequestSequenceServiceImpl(AtomicLong sequence, Map<String, Long> correlationMapStart) {
        this.sequence = sequence;
        this.correlationMap = correlationMapStart;
    }

    @Override
    public CorrelationPair nextCorrelationPair() {
        long id = sequence.getAndIncrement();
        String uuid = getUUID(id);
        return new CorrelationPair(uuid, id);
    }

    private String getUUID(long id) {
        String uuid = null;
        for (int i = 0; i < 100; i++) {
            String tmp = UUID.randomUUID().toString();
            if (correlationMap.computeIfAbsent(tmp, s -> id) == id) {
                uuid = tmp;
                break;
            }
        }
        if (uuid == null) {
            throw new RuntimeException("Failed to generate UUID for correlationId for 100 tries.");
        }
        return uuid;
    }
}
