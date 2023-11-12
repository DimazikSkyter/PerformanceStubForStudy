package ru.study.stub.theatre;

import ru.study.stub.model.Event;
import ru.study.stub.theatre.model.TheatreSeatResponse;

import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public interface TheatreClient {

    Set<String> events() throws ExecutionException, InterruptedException, TimeoutException;

    TheatreSeatResponse seats(String event) throws ExecutionException, InterruptedException, TimeoutException;

    Event info(String name);
}
