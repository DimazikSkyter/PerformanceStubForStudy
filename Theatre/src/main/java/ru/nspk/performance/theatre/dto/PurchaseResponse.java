package ru.nspk.performance.theatre.dto;

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
