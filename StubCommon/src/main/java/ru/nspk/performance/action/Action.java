package ru.nspk.performance.action;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@JsonTypeInfo( use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CreateReserveAction.class),
        @JsonSubTypes.Type(value = PaymentLinkAction.class),
        @JsonSubTypes.Type(value = PaymentLinkResponseAction.class),
        @JsonSubTypes.Type(value = PaymentResultAction.class),
        @JsonSubTypes.Type(value = SendPaymentLinkToApiAction.class),
        @JsonSubTypes.Type(value = ReserveResponseAction.class)
})
@SuperBuilder
public abstract class Action {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

   protected Long transactionId;

    public byte[] getBytes() throws JsonProcessingException {
        return OBJECT_MAPPER.writeValueAsBytes(this);
    }
}
