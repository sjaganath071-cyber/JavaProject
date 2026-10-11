package com.team11.defense_logistics.model;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public class ResourceAllocation {

    private Long id;

    @NotNull(message = "Request ID is required")
    @Positive(message = "Request ID must be positive")
    private Long requestId;

    @Positive(message = "Request line item ID must be positive")
    private Long requestLineItemId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "Vehicle ID is required")
    @Positive(message = "Vehicle ID must be positive")
    private Long vehicleId;

    @NotBlank(message = "Resource name is required")
    private String resourceName;

    @NotBlank(message = "Allocated-to value is required")
    private String allocatedTo;

    @Pattern(regexp = "^(Allocated|Released|Completed|Cancelled)$",
            message = "Status must be Allocated, Released, Completed, or Cancelled")
    private String status = "Allocated";

    private LocalDateTime allocationDate;
    private LocalDateTime releaseDate;

    public ResourceAllocation() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
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

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getAllocatedTo() {
        return allocatedTo;
    }

    public void setAllocatedTo(String allocatedTo) {
        this.allocatedTo = allocatedTo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getAllocationDate() {
        return allocationDate;
    }

    public void setAllocationDate(LocalDateTime allocationDate) {
        this.allocationDate = allocationDate;
    }

    public LocalDateTime getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDateTime releaseDate) {
        this.releaseDate = releaseDate;
    }
}
