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
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

@Entity
@Table(name = "PERSONNEL")
public class Personnel {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "personnel_seq_gen")
    @SequenceGenerator(
            name = "personnel_seq_gen",
            sequenceName = "PERSONNEL_SEQ",
            allocationSize = 1
    )
    @Column(name = "PERSONNEL_ID")
    private Long personnelId;

    @NotBlank
    @Column(name = "PERSONNEL_CODE", nullable = false, length = 30)
    private String personnelCode;

    @NotBlank
    @Column(name = "FULL_NAME", nullable = false, length = 100)
    private String fullName;

    @NotBlank
    @Column(name = "ROLE", nullable = false, length = 50)
    private String role;

    @NotNull
    @Positive
    @Column(name = "UNIT_ID", nullable = false)
    private Long unitId;

    @Column(name = "POSTING_LOCATION", length = 100)
    private String postingLocation;

    @NotBlank
    @Column(name = "AVAILABILITY", nullable = false, length = 30)
    private String availability = "AVAILABLE";

    @Column(name = "ACTIVE", nullable = false)
    private boolean active = true;

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

    public Long getPersonnelId() {
        return personnelId;
    }

    public String getPersonnelCode() {
        return personnelCode;
    }

    public void setPersonnelCode(String personnelCode) {
        this.personnelCode = personnelCode;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Long getUnitId() {
        return unitId;
    }

    public void setUnitId(Long unitId) {
        this.unitId = unitId;
    }

    public String getPostingLocation() {
        return postingLocation;
    }

    public void setPostingLocation(String postingLocation) {
        this.postingLocation = postingLocation;
    }

    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}