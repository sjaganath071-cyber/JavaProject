package com.team11.defense_logistics.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AssetDto {

    private Long id;

    @NotBlank(message = "Asset tag is required")
    @Size(max = 50, message = "Asset tag must not exceed 50 characters")
    private String assetTag;

    @NotBlank(message = "Asset name is required")
    @Size(max = 100, message = "Asset name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "Category is required")
    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category;

    @NotBlank(message = "Unit is required")
    @Size(max = 100, message = "Unit must not exceed 100 characters")
    private String unit;

    @NotBlank(message = "Location is required")
    @Size(max = 150, message = "Location must not exceed 150 characters")
    private String location;

    @NotBlank(message = "Condition is required")
    private String condition;

    @NotBlank(message = "Status is required")
    private String status;

    public AssetDto() {
    }

    public AssetDto(
            Long id,
            String assetTag,
            String name,
            String category,
            String unit,
            String location,
            String condition,
            String status) {

        this.id = id;
        this.assetTag = assetTag;
        this.name = name;
        this.category = category;
        this.unit = unit;
        this.location = location;
        this.condition = condition;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAssetTag() {
        return assetTag;
    }

    public void setAssetTag(String assetTag) {
        this.assetTag = assetTag;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
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
}
