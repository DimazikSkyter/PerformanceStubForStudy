package ru.nspk.performance.transactionshandler.keyvaluestorage;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public interface KeyValueStorage<K, V> {

    void put(String map, K key, V value) throws JsonProcessingException;

    V get(String map, K key);
}
