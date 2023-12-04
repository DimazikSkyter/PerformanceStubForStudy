package ru.nspk.performance.keyvaluestorage;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableFactory;
import ru.nspk.performance.keyvaluestorage.model.Person;

public class KeyValuePortableFactory implements PortableFactory {

    public static final int FACTORY_ID = 1;

    @Override
    public Portable create(int classId) {
        if (classId == Person.ID) {
            return new Person();
        }
        return null;
    }
}
