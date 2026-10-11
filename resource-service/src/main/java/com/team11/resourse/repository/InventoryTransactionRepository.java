package com.team11.resourse.repository;

import com.team11.resourse.entity.InventoryTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {

    List<InventoryTransaction> findByItemItemIdOrderByCreatedAtDesc(Long itemId);

    List<InventoryTransaction> findAllByOrderByCreatedAtDesc();

    boolean existsByItemItemId(Long itemId);
}