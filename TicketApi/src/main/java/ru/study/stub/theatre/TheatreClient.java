package ru.study.stub.theatre;

import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public interface TheatreClient {

    Set<String> events() throws ExecutionException, InterruptedException, TimeoutException;

    Set<String> seats(String event) throws ExecutionException, InterruptedException, TimeoutException;
}
