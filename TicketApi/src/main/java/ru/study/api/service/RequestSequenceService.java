package ru.study.api.service;

import ru.study.api.model.CorrelationPair;

public interface RequestSequenceService {

    CorrelationPair nextCorrelationPair();

    long requestIdByCorrelationUid(String correlationUid);
}
