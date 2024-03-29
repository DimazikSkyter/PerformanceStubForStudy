package ru.nspk.performance.transactionshandler.theatreclient;


import ru.nspk.performance.action.NotifyTheatreAction;
import ru.nspk.performance.theatre.dto.PurchaseResponse;

import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

//todo переделать на контракт
//сделать через интерфейс ретрофита
public interface TheatreClient {

    //todo #task1 - make single event
    //todo #task2 - make return event, not name
    Set<String> events() throws ExecutionException, InterruptedException, TimeoutException;

    Set<String> seats(String event) throws ExecutionException, InterruptedException, TimeoutException;

    void reserve(String requestId,
                 String event,
                 String seats,
                 Consumer<String> callback);

    void release(long reserveId);

    void purchase(NotifyTheatreAction notifyTheatreAction, Consumer<String> callback) throws ExecutionException, InterruptedException, TimeoutException;
}
