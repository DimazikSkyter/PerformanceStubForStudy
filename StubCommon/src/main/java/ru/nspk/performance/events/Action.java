package ru.nspk.performance.events;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@JsonTypeInfo( use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CreateReserveAction.class),
        @JsonSubTypes.Type(value = PaymentAction.class),
        @JsonSubTypes.Type(value = ReserveResponseAction.class)
})
@SuperBuilder
public abstract class Action {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

    protected long eventId;

    public byte[] getBytes() throws JsonProcessingException {
        return OBJECT_MAPPER.writeValueAsBytes(this);
    }
}
