package ru.nspk.performance.transactionshandler.keyvaluestorage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.jet.datamodel.Tuple2;
import com.hazelcast.map.IMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.nspk.performance.transactionshandler.properties.ImdgProperties;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class HazelcastKeyValue<K, V> {

    private final IMap<K, V> map;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ImdgProperties imdgProperties;


    public void put(K key, V value, Consumer<? super V> afterPutFunction) throws JsonProcessingException, UnsupportedEncodingException {

        map.putAsync(key,
                        value,
                        imdgProperties.getTimeoutMs(),
                        TimeUnit.MILLISECONDS)
                .thenAccept(afterPutFunction);
    }

    public Tuple2<Boolean, V> updateWithCondition(K key,
                                              Function<V, V> updateFunction,
                                              Function<V, Boolean> conditionFunction) {
        V newValue = null;
        V currentValue = map.get(key);
        boolean isValidState = conditionFunction.apply(currentValue);
        if (isValidState) {
            newValue = updateFunction.apply(currentValue);
            map.put(key, newValue, imdgProperties.getTimeoutMs(), TimeUnit.MILLISECONDS);
        }
        return Tuple2.tuple2(isValidState, isValidState ? newValue : currentValue);
    }

    public V get(K key) throws ExecutionException, InterruptedException, TimeoutException {
        return map.getAsync(key)
                .toCompletableFuture()
                .get(imdgProperties.getTimeoutMs(), TimeUnit.MILLISECONDS);
    }

    public CompletionStage<V> getAsync(K key) {
        return map.getAsync(key);
    }
}
