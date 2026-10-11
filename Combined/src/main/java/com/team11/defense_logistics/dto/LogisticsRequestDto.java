package com.team11.defense_logistics.dto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class LogisticsRequestDto {

    private Long id;

    @NotBlank(message = "Request code is required")
    @Size(max = 50, message = "Request code cannot exceed 50 characters")
    private String requestCode;

    @NotBlank(message = "Requesting unit is required")
    @Size(max = 100, message = "Requesting unit cannot exceed 100 characters")
    private String requestingUnit;

    @NotBlank(message = "Request type is required")
    private String requestType;

    @NotBlank(message = "Description is required")
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @NotBlank(message = "Priority is required")
    private String priority = "MEDIUM";

    @NotBlank(message = "Status is required")
    private String status = "PENDING";

    @NotNull(message = "Requested date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate requestedDate = LocalDate.now();

    @NotNull(message = "Required-by date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate requiredByDate;

    @Size(max = 100, message = "Assigned resource cannot exceed 100 characters")
    private String assignedResource;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;

    public LogisticsRequestDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRequestCode() {
        return requestCode;
    }

    public void setRequestCode(String requestCode) {
        this.requestCode = requestCode;
    }

    public String getRequestingUnit() {
        return requestingUnit;
    }

    public void setRequestingUnit(String requestingUnit) {
        this.requestingUnit = requestingUnit;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getRequestedDate() {
        return requestedDate;
    }

    public void setRequestedDate(LocalDate requestedDate) {
        this.requestedDate = requestedDate;
    }

    public LocalDate getRequiredByDate() {
        return requiredByDate;
    }

    public void setRequiredByDate(LocalDate requiredByDate) {
        this.requiredByDate = requiredByDate;
    }

    public String getAssignedResource() {
        return assignedResource;
    }

    public void setAssignedResource(String assignedResource) {
        this.assignedResource = assignedResource;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
