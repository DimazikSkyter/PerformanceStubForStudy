package ru.study.stub.service;

import ru.study.stub.model.CorrelationPair;

public interface RequestSequenceService {

    CorrelationPair nextCorrelationPair();
}
