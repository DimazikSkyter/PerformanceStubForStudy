package ru.nspk.performance.transactionshandler.theatreclient;


import ru.nspk.performance.theatre.model.PurchaseResponse;
import ru.nspk.performance.theatre.model.ReserveResponse;

import javax.security.auth.callback.Callback;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

//todo переделать на контракт
//сделать через интерфейс ретрофита
public interface TheatreClient {

    Set<String> events();

    Set<String> seats(String event);

    ReserveResponse reserve(String requestId,
                            long event,
                            List<String> seats,
                            Consumer<ReserveResponse> callback);

    void release(long reserveId);

    PurchaseResponse purchase(long reserveId);
}
