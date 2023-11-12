package ru.study.stub.service;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.study.stub.model.Person;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final int maxTries = 10;
    //todo вынести в хазелкаст и базу
    private final Map<String, Person> persons = new HashMap<>();

    @Override
    public String userIdOfPerson(Person person) {
        log.info("Get user id for person {}", person);
        Map.Entry<String, Person> personEntry = persons.entrySet().stream()
                .filter(entry ->
                        entry.getValue().getPassportCode().equals(person.getPassportCode())
                )
                .findFirst()
                .orElse(null);
        if (personEntry != null) {
            if (!personEntry.getValue().getFio().equals(person.getFio())) {
                throw new RuntimeException("Person incorrect data");
            }

            if (personEntry.getValue().getAge() != person.getAge()) {
                personEntry.getValue().setAge(person.getAge());
            }

            if (person.getAddress() != null && !person.getAddress().equals(personEntry.getValue().getAddress())) {
                personEntry.getValue().setAddress(person.getAddress());
            }
            return personEntry.getKey();
        }
        @NonNull String uuid = uuid(person);
        persons.put(uuid, person);
        return uuid;
    }

    private String uuid(Person person) {
        String uuid = null;
        for (int i = 0; i < maxTries; i++) {
            uuid = UUID.randomUUID().toString();
            if (!persons.containsKey(uuid)) {
                break;
            }
        }
        if (uuid == null) {
            throw new NullPointerException("Failed to generate uuid for max tries for user " + person);
        }
        return uuid;
    }
}
