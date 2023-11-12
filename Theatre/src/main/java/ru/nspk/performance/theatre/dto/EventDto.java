package ru.nspk.performance.theatre.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventDto {

    private String name;
    private Date date;
    private String merchant;
    private String type;
    private boolean exists;
}
