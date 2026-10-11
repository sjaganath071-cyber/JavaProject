package com.team11.logistics.entity;

import com.team11.logistics.model.RequestPriority;
import com.team11.logistics.model.RequestStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "LOGISTICS_REQUESTS")
public class LogisticsRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Request number is required")
    @Column(name = "REQUEST_NUMBER", nullable = false, unique = true, length = 50)
    private String requestNumber;

    @NotBlank(message = "Requesting unit is required")
    @Size(max = 150, message = "Requesting unit must not exceed 150 characters")
    @Column(name = "REQUESTING_UNIT", nullable = false, length = 150)
    private String requestingUnit;

    @Size(max = 150, message = "Requester name must not exceed 150 characters")
    @Column(name = "REQUESTER_NAME", length = 150)
    private String requesterName;

    @NotBlank(message = "Purpose is required")
    @Size(max = 1000, message = "Purpose must not exceed 1000 characters")
    @Column(name = "PURPOSE", nullable = false, length = 1000)
    private String purpose;

    @NotNull(message = "Priority is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "PRIORITY", nullable = false, length = 20)
    private RequestPriority priority;

    @Column(name = "REQUIRED_FROM")
    private LocalDate requiredFrom;

    @Column(name = "REQUIRED_UNTIL")
    private LocalDate requiredUntil;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private RequestStatus status = RequestStatus.DRAFT;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RequestLineItem> items = new ArrayList<>();

    public LogisticsRequest() {
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
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

    public List<RequestLineItem> getItems() {
        return items;
    }

    public void setItems(List<RequestLineItem> items) {
        this.items = items;
        if (this.items != null) {
            this.items.forEach(item -> item.setRequest(this));
        }
    }

    public void addItem(RequestLineItem item) {
        items.add(item);
        item.setRequest(this);
    }

    public void removeItem(RequestLineItem item) {
        items.remove(item);
        item.setRequest(null);
    }
}
