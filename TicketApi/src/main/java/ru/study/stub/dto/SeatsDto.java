package ru.study.stub.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SeatsDto {

    List<Map<String, Boolean>> seats;
    String errorMessage;
}
