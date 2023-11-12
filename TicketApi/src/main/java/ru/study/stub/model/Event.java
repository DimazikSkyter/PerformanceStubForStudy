package ru.study.stub.model;


import lombok.Data;
import lombok.NonNull;

import java.util.Date;

@Data
public class Event {

    private EventLevel eventLevel;
    @NonNull private String eventName;
    @NonNull private Date date;
    @NonNull private String merchant;
    @NonNull private String type;
}
