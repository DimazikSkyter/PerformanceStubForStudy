package ru.nspk.performance.transactionshandler.model;

import lombok.Data;

import java.util.List;

@Data
//todo перенести эвенты в commons
public abstract class Event {

    private long eventId;

    public byte[] getBytes() {
        return null;
    }
}
