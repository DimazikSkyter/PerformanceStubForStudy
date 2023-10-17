package ru.nspk.performance.events;

import lombok.Data;

import java.util.List;

@Data
public class CreateReserveAction extends Action {

    private final List<String> seats;
}
