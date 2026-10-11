package com.team11.resourse.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "INVENTORY_TRANSACTIONS")
public class InventoryTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inventory_transaction_seq_gen")
    @SequenceGenerator(name = "inventory_transaction_seq_gen", sequenceName = "INVENTORY_TRANSACTION_SEQ", allocationSize = 1)
    @Column(name = "TRANSACTION_ID")
    private Long transactionId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ITEM_ID", nullable = false)
    private InventoryItem item;

    @NotBlank
    @Column(name = "TRANSACTION_TYPE", nullable = false, length = 20)
    private String transactionType;

    @NotNull
    @Column(name = "QUANTITY", nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;

    @NotBlank
    @Column(name = "REASON", nullable = false, length = 500)
    private String reason;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getTransactionId() { return transactionId; }
    public InventoryItem getItem() { return item; }
    public void setItem(InventoryItem item) { this.item = item; }
    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}