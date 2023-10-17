package ru.nspk.performance.users;


import lombok.Data;

@Data
public class User {

    private long id;
    private String uuid;
    private String name;
    private String surname;
}
