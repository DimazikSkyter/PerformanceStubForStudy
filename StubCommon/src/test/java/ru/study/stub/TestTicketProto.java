package ru.study.stub;

import com.google.protobuf.CodedOutputStream;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.study.stub.proto.Ticket;
import ru.study.stub.proto.TicketModel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

public class TestTicketProto {


    @Test
    void createProto() throws IOException {
        Ticket ticket = Ticket.newBuilder()
                .setFio("fio")
                .setAge(13)
                .setAddress("address address")
                .setPrice(1.1)
                .setCreation(Instant.now().getEpochSecond())
                .setUidToPay(UUID.randomUUID().toString())
                .setEventName("Film")
                .setEventType(Ticket.EventType.HIGH)
                .build();

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        ticket.writeTo(byteArrayOutputStream);

        byteArrayOutputStream.close();

        Ticket ticket1 = Ticket.parseFrom(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));

        Assertions.assertThat(ticket1).isEqualTo(ticket);
    }
}
