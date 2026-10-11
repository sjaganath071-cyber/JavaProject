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
import jakarta.persistence.Transient;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "INVENTORY_ITEMS")
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inventory_item_seq_gen")
    @SequenceGenerator(name = "inventory_item_seq_gen", sequenceName = "INVENTORY_ITEM_SEQ", allocationSize = 1)
    @Column(name = "ITEM_ID")
    private Long itemId;

    @NotBlank
    @Column(name = "ITEM_CODE", nullable = false, length = 50)
    private String itemCode;

    @NotBlank
    @Column(name = "ITEM_NAME", nullable = false, length = 100)
    private String itemName;

    @NotBlank
    @Column(name = "CATEGORY", nullable = false, length = 50)
    private String category;

    @NotBlank
    @Column(name = "UNIT_OF_MEASURE", nullable = false, length = 30)
    private String unitOfMeasure;

    @NotNull
    @Positive
    @Column(name = "UNIT_ID", nullable = false)
    private Long unitId;

    @NotBlank
    @Column(name = "LOCATION", nullable = false, length = 100)
    private String location;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @Column(name = "QUANTITY_ON_HAND", nullable = false, precision = 19, scale = 4)
    private BigDecimal quantityOnHand = BigDecimal.ZERO;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @Column(name = "QUANTITY_RESERVED", nullable = false, precision = 19, scale = 4)
    private BigDecimal quantityReserved = BigDecimal.ZERO;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @Column(name = "REORDER_THRESHOLD", nullable = false, precision = 19, scale = 4)
    private BigDecimal reorderThreshold = BigDecimal.ZERO;

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

    @Transient
    public BigDecimal getQuantityAvailable() {
        return quantityOnHand.subtract(quantityReserved);
    }

    public Long getItemId() { return itemId; }
    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getUnitOfMeasure() { return unitOfMeasure; }
    public void setUnitOfMeasure(String unitOfMeasure) { this.unitOfMeasure = unitOfMeasure; }
    public Long getUnitId() { return unitId; }
    public void setUnitId(Long unitId) { this.unitId = unitId; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public BigDecimal getQuantityOnHand() { return quantityOnHand; }
    public void setQuantityOnHand(BigDecimal quantityOnHand) { this.quantityOnHand = quantityOnHand; }
    public BigDecimal getQuantityReserved() { return quantityReserved; }
    public void setQuantityReserved(BigDecimal quantityReserved) { this.quantityReserved = quantityReserved; }
    public BigDecimal getReorderThreshold() { return reorderThreshold; }
    public void setReorderThreshold(BigDecimal reorderThreshold) { this.reorderThreshold = reorderThreshold; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}