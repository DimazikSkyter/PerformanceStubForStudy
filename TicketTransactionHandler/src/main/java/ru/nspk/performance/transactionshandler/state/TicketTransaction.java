package ru.nspk.performance.transactionshandler.state;

import lombok.Data;
import ru.nspk.performance.transactionshandler.model.Event;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.Semaphore;

@Data
public class TicketTransaction implements Serializable {

    private List<Event> event;
    private TransactionState transactionState;

    public void lock() {
        //todo Реализовать однопоточную обработку тикеттранзакшена
    }

    public void release() {
        //todo
    }

    public byte[] getBytes() {
        return new byte[1];
    }
}
