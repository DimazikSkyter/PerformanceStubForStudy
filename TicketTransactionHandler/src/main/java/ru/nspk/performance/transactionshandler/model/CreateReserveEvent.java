package ru.nspk.performance.transactionshandler.model;

import lombok.Data;

import java.util.List;

@Data
public class CreateReserveEvent extends Event {

    private final List<String> seats;
}
