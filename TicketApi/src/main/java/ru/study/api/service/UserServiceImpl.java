package ru.study.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.nspk.performance.keyvaluestorage.hazelcast.HazelcastManager;
import ru.nspk.performance.keyvaluestorage.model.Person;

import java.io.UnsupportedEncodingException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    public static final String PERSON_DATA_MAP = "person_data";
    public static final String PASSPORT_PERSON_MAP = "passport_person";

    private final int maxTries = 10;
    //todo добавить базу
    private final HazelcastManager hazelcastManager;

    @Override
    public @NonNull String userIdOfPerson(@NonNull Person person) throws ExecutionException, InterruptedException, TimeoutException, UnsupportedEncodingException, JsonProcessingException {
        log.info("Get user id for person {}", person);

        if (person.getPassportCode().isEmpty()) {
            throw new RuntimeException("Person passport code is empty.");
        }

        String userUuid = hazelcastManager.get(PASSPORT_PERSON_MAP, person.getPassportCode());
        if (userUuid == null) {
            return saveNewPerson(person);
        }
        Person currentData = hazelcastManager.get(PERSON_DATA_MAP, userUuid);
        if (currentData != null) {
            if (!currentData.getFio().equals(person.getFio())) {
                throw new RuntimeException("Person incorrect data");
            }
            boolean diffEnabled = true;
            if (currentData.getAge() != person.getAge()) {
                currentData.setAge(person.getAge());
                diffEnabled = true;
            }

            if (person.getAddress() != null && !person.getAddress().equals(currentData.getAddress())) {
                currentData.setAddress(person.getAddress());
                diffEnabled = true;
            }
            if (diffEnabled) {
                updatePerson(currentData, userUuid);
            }
            return userUuid;
        }
        saveNewPerson(person, userUuid);
        return userUuid;
    }

    private void updatePerson(Person currentData, String userUuid) {
        int version = currentData.getVersion();
        currentData.setVersion(version + 1);
        hazelcastManager.<String, Person>updateWithCondition(PERSON_DATA_MAP, userUuid, oldPerson -> currentData, oldPerson -> oldPerson.getVersion() == version);
    }

    private String saveNewPerson(Person person) throws ExecutionException, InterruptedException, TimeoutException, UnsupportedEncodingException, JsonProcessingException {
        person.setVersion(1);
        String userUid = saveNewPerson(person, null);
        hazelcastManager.put(PASSPORT_PERSON_MAP, person.getPassportCode(), userUid, s -> {});
        return userUid;
    }

    private String saveNewPerson(Person person, String userUuid) throws ExecutionException, InterruptedException, TimeoutException, UnsupportedEncodingException, JsonProcessingException {
        if (userUuid == null) {
            userUuid = uuid(person);
        }
        hazelcastManager.put(PERSON_DATA_MAP, userUuid, person, person1 -> {});
        return userUuid;
    }

    private String uuid(Person person) throws ExecutionException, InterruptedException, TimeoutException {
        String uuid = null;
        for (int i = 0; i < maxTries; i++) {
            uuid = UUID.randomUUID().toString();
            Person unknownPerson = hazelcastManager.get(PERSON_DATA_MAP, uuid);
            if (unknownPerson == null) {
                break;
            }
        }
        if (uuid == null) {
            throw new NullPointerException("Failed to generate uuid for max tries for user " + person);
        }
        return uuid;
    }
}
