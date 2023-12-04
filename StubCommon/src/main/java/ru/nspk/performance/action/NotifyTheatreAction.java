package ru.nspk.performance.action;

import lombok.Data;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Data
@SuperBuilder
@Jacksonized
public class NotifyTheatreAction extends Action {

    private Long reserveId;
    private String requestId;

    protected NotifyTheatreAction(ActionBuilder<?, ?> b) {
        super(b);
    }
}
