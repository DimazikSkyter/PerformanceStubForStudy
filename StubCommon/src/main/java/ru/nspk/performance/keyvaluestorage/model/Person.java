package ru.nspk.performance.keyvaluestorage.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import lombok.*;
import ru.nspk.performance.keyvaluestorage.KeyValuePortableFactory;

import java.io.IOException;
import java.util.Objects;

@Data
@Builder
@NoArgsConstructor
public class Person implements Portable {

    public static final int ID = 5;
//    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @JsonCreator
    public Person(
            @JsonProperty("fio") @NonNull String fio,
            @JsonProperty("passportCode") @NonNull String passportCode,
            @JsonProperty("age") int age,
            @JsonProperty("address") String address,
            @JsonProperty("version") int version
    ) {
        this.fio = fio;
        this.passportCode = passportCode;
        this.age = age;
        this.address = address;
        this.version = version;
    }


    @NonNull private String fio;
    @NonNull private String passportCode;
    private int age;
    private String address;
    private int version;

    @Override
    public int getFactoryId() {
        return KeyValuePortableFactory.FACTORY_ID;
    }

    @Override
    public int getClassId() {
        return ID;
    }

    @Override
    public void writePortable(PortableWriter writer) throws IOException {
        writer.writeString("fio", this.fio);
        writer.writeString("passportCode", this.passportCode);
        writer.writeString("address", this.address);
        writer.writeInt("version", this.version);
        writer.writeInt("age", this.age);
    }

    @Override
    public void readPortable(PortableReader reader) throws IOException {
        this.fio = Objects.requireNonNull(reader.readString("fio"));
        this.passportCode = Objects.requireNonNull(reader.readString("passportCode"));
        this.address = reader.readString("address");
        this.version = reader.readInt("version");
        this.age = reader.readInt("age");
    }
}
