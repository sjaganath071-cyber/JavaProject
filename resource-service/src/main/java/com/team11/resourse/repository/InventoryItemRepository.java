package com.team11.resourse.repository;

import com.team11.resourse.entity.InventoryItem;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long>, JpaSpecificationExecutor<InventoryItem> {

    Optional<InventoryItem> findByItemCodeIgnoreCase(String itemCode);

    boolean existsByItemCodeIgnoreCase(String itemCode);

    List<InventoryItem> findByUnitId(Long unitId);

    List<InventoryItem> findByCategoryIgnoreCase(String category);

    List<InventoryItem> findByLocationIgnoreCase(String location);

    List<InventoryItem> findByItemNameContainingIgnoreCase(String itemName);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select item from InventoryItem item where item.itemId = :itemId")
    Optional<InventoryItem> findByIdForUpdate(@Param("itemId") Long itemId);

    @Query("select item from InventoryItem item where item.quantityOnHand - item.quantityReserved <= item.reorderThreshold")
    List<InventoryItem> findLowStockItems();
}