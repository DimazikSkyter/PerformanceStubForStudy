package ru.nspk.performance.action;


import lombok.Data;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;

@Data
@SuperBuilder
@Jacksonized
public class SendPaymentLinkToApiAction extends Action {

    private long requestId;
    private Instant createdTime;

    protected SendPaymentLinkToApiAction(ActionBuilder<?, ?> b) {
        super(b);
    }
}
