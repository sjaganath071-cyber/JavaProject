package com.team11.resourse.repository;

import com.team11.resourse.entity.MaintenanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MaintenanceRecordRepository extends JpaRepository<MaintenanceRecord, Long>, JpaSpecificationExecutor<MaintenanceRecord> {

    List<MaintenanceRecord> findByResourceTypeIgnoreCaseAndResourceIdOrderByScheduledDateAsc(
            String resourceType, Long resourceId);

    List<MaintenanceRecord> findByStatusIgnoreCaseOrderByScheduledDateAsc(String status);

    List<MaintenanceRecord> findAllByOrderByScheduledDateAsc();

    @Query("select record from MaintenanceRecord record where upper(record.status) = 'SCHEDULED' and record.scheduledDate >= :now order by record.scheduledDate asc")
    List<MaintenanceRecord> findUpcomingScheduled(@Param("now") LocalDateTime now);

    @Query("select record from MaintenanceRecord record where upper(record.status) = 'SCHEDULED' and record.scheduledDate < :now order by record.scheduledDate asc")
    List<MaintenanceRecord> findOverdueScheduled(@Param("now") LocalDateTime now);
}