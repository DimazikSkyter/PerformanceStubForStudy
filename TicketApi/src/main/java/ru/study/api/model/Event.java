package ru.study.api.model;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    @NonNull private String title;
    private String name;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @NonNull private Date date;
    private String merchant;
    @NonNull private String type;
    @NonNull private boolean exists;
}
