package ru.study.api.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;


@JsonInclude(JsonInclude.Include.NON_NULL)
public record EventDto (String title, @NonNull String eventName, @NonNull @JsonFormat(pattern="yyyy-MM-dd", timezone = "Europe/Moscow") Date eventDate) {}