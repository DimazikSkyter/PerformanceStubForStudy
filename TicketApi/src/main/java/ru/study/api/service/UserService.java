package ru.study.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.NonNull;
import ru.nspk.performance.keyvaluestorage.model.Person;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public interface UserService {

    @NonNull String userIdOfPerson(Person person) throws ExecutionException, InterruptedException, TimeoutException, UnsupportedEncodingException, JsonProcessingException;
}
