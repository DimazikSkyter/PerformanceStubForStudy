package ru.nspk.performance.transactionshandler.keyvaluestorage;

import lombok.RequiredArgsConstructor;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class KeyValueStorageImpl<K> implements KeyValueStorage<K, String> {

    @Override
    public CompletableFuture<String> put(K key, String value) {
        return null;
    }

    @Override
    public CompletableFuture<String> get(K key) {
        return null;
    }
}
