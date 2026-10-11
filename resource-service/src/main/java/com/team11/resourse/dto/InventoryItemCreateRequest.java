package com.team11.resourse.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record InventoryItemCreateRequest(
        @NotBlank(message = "Item code is required") @Size(max = 50) String itemCode,
        @NotBlank(message = "Item name is required") @Size(max = 100) String itemName,
        @NotBlank(message = "Category is required") @Size(max = 50) String category,
        @NotBlank(message = "Unit of measure is required") @Size(max = 30) String unitOfMeasure,
        @NotNull(message = "Unit ID is required") @Positive(message = "Unit ID must be greater than zero") Long unitId,
        @NotBlank(message = "Location is required") @Size(max = 100) String location,
        @NotNull(message = "Quantity on hand is required") @DecimalMin(value = "0.0", message = "Quantity on hand cannot be negative") BigDecimal quantityOnHand,
        @DecimalMin(value = "0.0", message = "Quantity reserved cannot be negative") BigDecimal quantityReserved,
        @DecimalMin(value = "0.0", message = "Reorder threshold cannot be negative") BigDecimal reorderThreshold
) {
}