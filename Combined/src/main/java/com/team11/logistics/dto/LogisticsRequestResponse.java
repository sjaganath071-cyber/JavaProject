package com.team11.logistics.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.team11.logistics.model.RequestPriority;
import com.team11.logistics.model.RequestStatus;
import com.team11.logistics.model.ResourceType;

public class LogisticsRequestResponse {

    private Long id;
    private String requestNumber;
    private String requestingUnit;
    private String requesterName;
    private String purpose;
    private RequestPriority priority;
    private LocalDate requiredFrom;
    private LocalDate requiredUntil;
    private RequestStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<RequestLineItemResponse> items = new ArrayList<>();

    public LogisticsRequestResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRequestNumber() {
        return requestNumber;
    }

    public void setRequestNumber(String requestNumber) {
        this.requestNumber = requestNumber;
    }

    public String getRequestingUnit() {
        return requestingUnit;
    }

    public void setRequestingUnit(String requestingUnit) {
        this.requestingUnit = requestingUnit;
    }

    public String getRequesterName() {
        return requesterName;
    }

    public void setRequesterName(String requesterName) {
        this.requesterName = requesterName;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public RequestPriority getPriority() {
        return priority;
    }

    public void setPriority(RequestPriority priority) {
        this.priority = priority;
    }

    public LocalDate getRequiredFrom() {
        return requiredFrom;
    }

    public void setRequiredFrom(LocalDate requiredFrom) {
        this.requiredFrom = requiredFrom;
    }

    public LocalDate getRequiredUntil() {
        return requiredUntil;
    }

    public void setRequiredUntil(LocalDate requiredUntil) {
        this.requiredUntil = requiredUntil;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<RequestLineItemResponse> getItems() {
        return items;
    }

    public void setItems(List<RequestLineItemResponse> items) {
        this.items = items;
    }

    public static class RequestLineItemResponse {
        private Long id;
        private ResourceType resourceType;
        private String resourceReference;
        private Integer quantity;
        private String notes;
        private Integer fulfilledQuantity;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public ResourceType getResourceType() {
            return resourceType;
        }

        public void setResourceType(ResourceType resourceType) {
            this.resourceType = resourceType;
        }

        public String getResourceReference() {
            return resourceReference;
        }

        public void setResourceReference(String resourceReference) {
            this.resourceReference = resourceReference;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }

        public Integer getFulfilledQuantity() {
            return fulfilledQuantity;
        }

        public void setFulfilledQuantity(Integer fulfilledQuantity) {
            this.fulfilledQuantity = fulfilledQuantity;
        }
    }
}
