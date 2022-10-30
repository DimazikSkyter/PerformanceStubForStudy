package ru.study.stub.model;


import lombok.Data;

@Data
public class Event {

    private EventLevel eventLevel;
    private String eventName;
    private double price;
}
