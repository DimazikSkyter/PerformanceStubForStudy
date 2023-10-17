package ru.nspk.performance.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

@Data
public abstract class Action {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private long eventId;

    public byte[] getBytes() throws JsonProcessingException {
        return OBJECT_MAPPER.writeValueAsBytes(this);
    }
}
