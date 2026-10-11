package com.team11.resourse.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

@Entity
@Table(name = "MAINTENANCE_RECORDS")
public class MaintenanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "maintenance_seq_gen")
    @SequenceGenerator(name = "maintenance_seq_gen", sequenceName = "MAINTENANCE_SEQ", allocationSize = 1)
    @Column(name = "MAINTENANCE_ID")
    private Long maintenanceId;

    @NotBlank
    @Column(name = "RESOURCE_TYPE", nullable = false, length = 30)
    private String resourceType;

    @NotNull
    @Positive
    @Column(name = "RESOURCE_ID", nullable = false)
    private Long resourceId;

    @NotBlank
    @Column(name = "MAINTENANCE_TYPE", nullable = false, length = 50)
    private String maintenanceType;

    @NotBlank
    @Column(name = "DESCRIPTION", nullable = false, length = 1000)
    private String description;

    @NotNull
    @Column(name = "SCHEDULED_DATE", nullable = false)
    private LocalDateTime scheduledDate;

    @Column(name = "START_DATE")
    private LocalDateTime startDate;

    @Column(name = "COMPLETION_DATE")
    private LocalDateTime completionDate;

    @NotBlank
    @Pattern(regexp = "^(SCHEDULED|IN_PROGRESS|COMPLETED|CANCELLED)$")
    @Column(name = "STATUS", nullable = false, length = 20)
    private String status = "SCHEDULED";

    @Column(name = "SERVICE_NOTES", length = 2000)
    private String serviceNotes;

    @Column(name = "TECHNICIAN", length = 100)
    private String technician;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;

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

    public Long getMaintenanceId() { return maintenanceId; }
    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }
    public Long getResourceId() { return resourceId; }
    public void setResourceId(Long resourceId) { this.resourceId = resourceId; }
    public String getMaintenanceType() { return maintenanceType; }
    public void setMaintenanceType(String maintenanceType) { this.maintenanceType = maintenanceType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(LocalDateTime scheduledDate) { this.scheduledDate = scheduledDate; }
    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
    public LocalDateTime getCompletionDate() { return completionDate; }
    public void setCompletionDate(LocalDateTime completionDate) { this.completionDate = completionDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getServiceNotes() { return serviceNotes; }
    public void setServiceNotes(String serviceNotes) { this.serviceNotes = serviceNotes; }
    public String getTechnician() { return technician; }
    public void setTechnician(String technician) { this.technician = technician; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}