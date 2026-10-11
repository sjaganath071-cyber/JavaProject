package com.team11.logistics.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class CreateAllocationDto {

    @NotNull(message = "Request line item ID is required")
    @Positive(message = "Request line item ID must be positive")
    private Long requestLineItemId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    public CreateAllocationDto() {
    }

    public Long getRequestLineItemId() {
        return requestLineItemId;
    }

    public void setRequestLineItemId(Long requestLineItemId) {
        this.requestLineItemId = requestLineItemId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
