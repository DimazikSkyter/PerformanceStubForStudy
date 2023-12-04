package ru.study.api.theatre;

import ru.study.api.dto.SeatResponse;
import ru.study.api.model.Event;

import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public interface TheatreClient {

    Set<String> events() throws ExecutionException, InterruptedException, TimeoutException;

    SeatResponse seats(String event) throws ExecutionException, InterruptedException, TimeoutException;

    Event info(String event) throws ExecutionException, InterruptedException, TimeoutException;
}
