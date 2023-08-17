package ru.nspk.performance.transactionshandler.keyvaluestorage;

import java.util.concurrent.CompletableFuture;

public interface KeyValueStorage <K, V> {

    CompletableFuture<V> put(String map, K key, V value);

    CompletableFuture<V> get(String map, K key);
}
