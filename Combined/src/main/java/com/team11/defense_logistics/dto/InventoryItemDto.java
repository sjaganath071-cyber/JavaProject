package com.team11.defense_logistics.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class InventoryItemDto {

    private Long id;

    @NotBlank(message = "Item code is required")
    @Size(max = 50, message = "Item code cannot exceed 50 characters")
    private String itemCode;

    @NotBlank(message = "Item name is required")
    @Size(max = 150, message = "Item name cannot exceed 150 characters")
    private String name;

    @NotBlank(message = "Category is required")
    private String category;

    @NotBlank(message = "Unit of measurement is required")
    private String unit;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;

    @NotNull(message = "Minimum stock is required")
    @Min(value = 0, message = "Minimum stock cannot be negative")
    private Integer minimumStock;

    @NotBlank(message = "Storage location is required")
    private String storageLocation;

    private String description;

    public InventoryItemDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
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

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getMinimumStock() {
        return minimumStock;
    }

    public void setMinimumStock(Integer minimumStock) {
        this.minimumStock = minimumStock;
    }

    public String getStorageLocation() {
        return storageLocation;
    }

    public void setStorageLocation(String storageLocation) {
        this.storageLocation = storageLocation;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStockStatus() {
        if (quantity == null || minimumStock == null) {
            return "UNKNOWN";
        }

        if (quantity == 0) {
            return "OUT_OF_STOCK";
        }

        if (quantity <= minimumStock) {
            return "LOW_STOCK";
        }

        return "IN_STOCK";
    }
}
