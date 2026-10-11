package com.team11.logistics.repository;

import com.team11.logistics.entity.ResourceAllocation;
import com.team11.logistics.model.AllocationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResourceAllocationRepository extends JpaRepository<ResourceAllocation, Long> {

    long countByStatus(AllocationStatus status);

    List<ResourceAllocation> findAllByOrderByAllocatedAtDesc();

    List<ResourceAllocation> findByRequestIdOrderByAllocatedAtDesc(Long requestId);

    List<ResourceAllocation> findByStatusOrderByAllocatedAtDesc(AllocationStatus status);

    List<ResourceAllocation> findByRequestLineItemIdOrderByAllocatedAtDesc(Long requestLineItemId);

    List<ResourceAllocation> findByRequestIdAndStatusOrderByAllocatedAtDesc(Long requestId, AllocationStatus status);
}
