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
@Table(name = "ASSETS")
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "asset_seq_gen")
    @SequenceGenerator(
            name = "asset_seq_gen",
            sequenceName = "ASSET_SEQ",
            allocationSize = 1
    )
    @Column(name = "ASSET_ID")
    private Long assetId;

    @NotBlank
    @Column(name = "ASSET_TAG", nullable = false, length = 50)
    private String assetTag;

    @NotBlank
    @Column(name = "ASSET_NAME", nullable = false, length = 100)
    private String assetName;

    @NotBlank
    @Column(name = "CATEGORY", nullable = false, length = 50)
    private String category;

    @NotNull
    @Positive
    @Column(name = "UNIT_ID", nullable = false)
    private Long unitId;

    @NotBlank
    @Column(name = "LOCATION", nullable = false, length = 100)
    private String location;

    @NotBlank
    @Column(name = "CONDITION_STATUS", nullable = false, length = 30)
    private String condition;

    @NotBlank
    @Column(name = "STATUS", nullable = false, length = 30)
    private String status;

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

    public Long getAssetId() {
        return assetId;
    }

    public String getAssetTag() {
        return assetTag;
    }

    public void setAssetTag(String assetTag) {
        this.assetTag = assetTag;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Long getUnitId() {
        return unitId;
    }

    public void setUnitId(Long unitId) {
        this.unitId = unitId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}