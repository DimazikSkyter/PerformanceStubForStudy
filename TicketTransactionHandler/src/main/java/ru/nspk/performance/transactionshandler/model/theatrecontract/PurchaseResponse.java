package ru.nspk.performance.transactionshandler.model.theatrecontract;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class PurchaseResponse {

    private boolean result;
    private String requestId;
}
