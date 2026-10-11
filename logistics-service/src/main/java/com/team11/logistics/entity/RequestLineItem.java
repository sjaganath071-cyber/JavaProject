package com.team11.logistics.entity;

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
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "LOGISTICS_REQUEST_ITEMS")
public class RequestLineItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "REQUEST_ID", nullable = false)
    private LogisticsRequest request;

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

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    @Column(name = "NOTES", length = 500)
    private String notes;

    @Column(name = "FULFILLED_QUANTITY", nullable = false)
    private Integer fulfilledQuantity = 0;

    public RequestLineItem() {
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
