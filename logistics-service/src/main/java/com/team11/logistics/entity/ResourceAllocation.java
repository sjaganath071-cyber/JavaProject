package com.team11.logistics.entity;

import com.team11.logistics.model.AllocationStatus;
import com.team11.logistics.model.ResourceType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
@Table(name = "LOGISTICS_ALLOCATIONS")
public class ResourceAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Request is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "REQUEST_ID", nullable = false)
    private LogisticsRequest request;

    @NotNull(message = "Request line item is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "REQUEST_LINE_ITEM_ID", nullable = false)
    private RequestLineItem requestLineItem;

    @NotNull(message = "Resource type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "RESOURCE_TYPE", nullable = false, length = 20)
    private ResourceType resourceType;

    @NotBlank(message = "Resource reference is required")
    @Size(max = 150, message = "Resource reference must not exceed 150 characters")
    @Column(name = "RESOURCE_REFERENCE", nullable = false, length = 150)
    private String resourceReference;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Column(name = "QUANTITY", nullable = false)
    private Integer quantity;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private AllocationStatus status = AllocationStatus.ALLOCATED;

    @NotNull(message = "Allocated time is required")
    @Column(name = "ALLOCATED_AT", nullable = false)
    private LocalDateTime allocatedAt;

    @Column(name = "RELEASED_AT")
    private LocalDateTime releasedAt;

    @Column(name = "COMPLETED_AT")
    private LocalDateTime completedAt;

    @Size(max = 200, message = "Reservation reference must not exceed 200 characters")
    @Column(name = "RESERVATION_REFERENCE", length = 200)
    private String reservationReference;

    public ResourceAllocation() {
    }

    @PrePersist
    protected void onCreate() {
        if (allocatedAt == null) {
            allocatedAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LogisticsRequest getRequest() {
        return request;
    }

    public void setRequest(LogisticsRequest request) {
        this.request = request;
    }

    public RequestLineItem getRequestLineItem() {
        return requestLineItem;
    }

    public void setRequestLineItem(RequestLineItem requestLineItem) {
        this.requestLineItem = requestLineItem;
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

    public AllocationStatus getStatus() {
        return status;
    }

    public void setStatus(AllocationStatus status) {
        this.status = status;
    }

    public LocalDateTime getAllocatedAt() {
        return allocatedAt;
    }

    public void setAllocatedAt(LocalDateTime allocatedAt) {
        this.allocatedAt = allocatedAt;
    }

    public LocalDateTime getReleasedAt() {
        return releasedAt;
    }

    public void setReleasedAt(LocalDateTime releasedAt) {
        this.releasedAt = releasedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public String getReservationReference() {
        return reservationReference;
    }

    public void setReservationReference(String reservationReference) {
        this.reservationReference = reservationReference;
    }
}
