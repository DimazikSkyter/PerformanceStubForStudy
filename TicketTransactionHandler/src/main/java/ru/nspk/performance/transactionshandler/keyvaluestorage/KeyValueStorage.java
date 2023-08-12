package ru.nspk.performance.transactionshandler.keyvaluestorage;

import java.util.concurrent.CompletableFuture;

public interface KeyValueStorage <K, V> {

    CompletableFuture<K> put(K key, V value);
}
