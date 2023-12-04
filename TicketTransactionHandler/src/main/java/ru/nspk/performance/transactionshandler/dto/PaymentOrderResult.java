package ru.nspk.performance.transactionshandler.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentOrderResult {

    private String requestId;
    private String status;

    public boolean isSuccess() {
        return "Success".equals(status);
    }
}
