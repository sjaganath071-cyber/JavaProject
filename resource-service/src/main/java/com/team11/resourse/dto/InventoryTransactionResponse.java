package com.team11.resourse.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InventoryTransactionResponse(
        Long transactionId,
        Long itemId,
        String transactionType,
        BigDecimal quantity,
        String reason,
        LocalDateTime createdAt
) {
}