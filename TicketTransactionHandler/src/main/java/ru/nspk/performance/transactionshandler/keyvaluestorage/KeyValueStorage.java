package ru.nspk.performance.transactionshandler.keyvaluestorage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hazelcast.jet.datamodel.Tuple2;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;

public interface KeyValueStorage {

    <K, V> void put(String map, K key, V value, Consumer<? super V> afterPutFunction) throws JsonProcessingException, UnsupportedEncodingException;

    <K> long getAndIncrement(String map, K key) throws InterruptedException;

    <K, V> Tuple2<Boolean, V> updateWithCondition(String map, K key, Function<V, V> updateFunction, Function<V, Boolean> conditionFunction);

    <K, V> CompletionStage<V> getAsync(String map, K key);

    <K, V> V get(String map, K key) throws ExecutionException, InterruptedException, TimeoutException;
}
