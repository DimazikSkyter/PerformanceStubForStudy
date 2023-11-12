package ru.study.stub.model;

import lombok.Data;
import lombok.NonNull;

@Data
public class Person {

    @NonNull private String fio;
    @NonNull private String passportCode;
    private int age;
    private String address;
}
