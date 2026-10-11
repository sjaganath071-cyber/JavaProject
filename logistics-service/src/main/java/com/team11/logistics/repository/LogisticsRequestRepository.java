package com.team11.logistics.repository;

import com.team11.logistics.entity.LogisticsRequest;
import com.team11.logistics.model.AllocationStatus;
import com.team11.logistics.model.RequestPriority;
import com.team11.logistics.model.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LogisticsRequestRepository extends JpaRepository<LogisticsRequest, Long>, JpaSpecificationExecutor<LogisticsRequest> {

    Optional<LogisticsRequest> findById(Long id);

    long countByStatus(RequestStatus status);

    @Query("select count(r) from LogisticsRequest r where r.status = :status and not exists " +
            "(select a from ResourceAllocation a where a.request = r and a.status = :activeStatus)")
    long countApprovedRequestsWithoutActiveAllocation(
            @Param("status") RequestStatus status,
            @Param("activeStatus") AllocationStatus activeStatus);

    boolean existsByRequestNumber(String requestNumber);

    List<LogisticsRequest> findByStatus(RequestStatus status);

    List<LogisticsRequest> findByRequestingUnitContainingIgnoreCase(String requestingUnit);

    List<LogisticsRequest> findByPriority(RequestPriority priority);

    List<LogisticsRequest> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
