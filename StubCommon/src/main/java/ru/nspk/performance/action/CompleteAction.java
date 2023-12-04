package ru.nspk.performance.action;


import lombok.Data;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Data
@SuperBuilder
@Jacksonized
public class CompleteAction extends Action {

    private boolean result;
    private String requestId;

    protected CompleteAction(ActionBuilder<?, ?> b) {
        super(b);
    }
}
