package ru.nspk.performance.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Data
@ToString(callSuper = true)
@SuperBuilder
@Jacksonized
public class CreateReserveAction extends Action {

    private final List<String> seats;
}
