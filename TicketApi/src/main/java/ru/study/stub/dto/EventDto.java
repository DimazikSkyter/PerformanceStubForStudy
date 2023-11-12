package ru.study.stub.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.Date;


@JsonInclude(JsonInclude.Include.NON_NULL)
public record EventDto (@NonNull String eventName, @NonNull Date eventDate) {}