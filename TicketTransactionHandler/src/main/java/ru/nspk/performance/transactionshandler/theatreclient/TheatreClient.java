package ru.nspk.performance.transactionshandler.theatreclient;


import ru.nspk.performance.theatre.model.PurchaseResponse;
import ru.nspk.performance.theatre.model.ReserveResponse;

import javax.security.auth.callback.Callback;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

//todo переделать на контракт
//сделать через интерфейс ретрофита
public interface TheatreClient {

    Set<String> events() throws ExecutionException, InterruptedException, TimeoutException;

    Set<String> seats(String event) throws ExecutionException, InterruptedException, TimeoutException;

    void reserve(String requestId,
                            long event,
                            List<String> seats,
                            Consumer<ReserveResponse> callback) throws ExecutionException, InterruptedException, TimeoutException;

    void release(long reserveId);

    PurchaseResponse purchase(long reserveId) throws ExecutionException, InterruptedException, TimeoutException;
}
