package ru.nspk.performance.transactionshandler.model;

import lombok.Data;

import java.util.List;

@Data
public abstract class Event {
    private long eventId;
}
