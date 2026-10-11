package com.team11.resourse.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InventoryItemResponse(
        Long itemId,
        String itemCode,
        String itemName,
        String category,
        String unitOfMeasure,
        Long unitId,
        String location,
        BigDecimal quantityOnHand,
        BigDecimal quantityReserved,
        BigDecimal quantityAvailable,
        BigDecimal reorderThreshold,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}