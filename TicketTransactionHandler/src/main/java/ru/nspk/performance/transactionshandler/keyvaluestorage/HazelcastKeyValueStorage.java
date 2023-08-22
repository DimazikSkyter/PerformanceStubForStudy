package ru.nspk.performance.transactionshandler.keyvaluestorage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.jet.impl.JetService;
import lombok.RequiredArgsConstructor;

import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class HazelcastKeyValueStorage<K, V> implements KeyValueStorage<K, V> {

    private final JetService jetService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Charset charset;

    @Override
    public void put(String map, K key, V value) throws JsonProcessingException {

        return jetService.getJetInstance().getHazelcastInstance()
                .<K, byte[]>getMap("ads")
                .tryPut(key, objectMapper.writeValueAsString(value).getBytes(charset),
                        1000, TimeUnit.SECONDS);
    }

    @Override
    public CompletableFuture<V> get(String map, K key) {
        return null;
    }
}
