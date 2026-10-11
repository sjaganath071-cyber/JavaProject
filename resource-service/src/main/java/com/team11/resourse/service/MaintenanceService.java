package com.team11.resourse.service;

import com.team11.resourse.dto.MaintenanceCreateRequest;
import com.team11.resourse.dto.MaintenanceNotesRequest;
import com.team11.resourse.dto.MaintenanceResponse;
import com.team11.resourse.dto.MaintenanceStatusRequest;
import com.team11.resourse.dto.MaintenanceUpdateRequest;
import com.team11.resourse.entity.MaintenanceRecord;
import com.team11.resourse.repository.MaintenanceRecordRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class MaintenanceService {

    private static final String VALID_STATUS_PATTERN = "^(SCHEDULED|IN_PROGRESS|COMPLETED|CANCELLED)$";

    private final MaintenanceRecordRepository maintenanceRepository;

    public MaintenanceService(MaintenanceRecordRepository maintenanceRepository) {
        this.maintenanceRepository = maintenanceRepository;
    }

    public MaintenanceResponse create(MaintenanceCreateRequest request) {
        if (request == null) {
            throw badRequest("Maintenance request is required");
        }
        validateFields(request.resourceType(), request.resourceId(), request.maintenanceType(),
                request.description(), request.scheduledDate(), request.startDate(), request.completionDate());
        String status = normalizeStatus(request.status());
        validateStatus(status);

        MaintenanceRecord record = new MaintenanceRecord();
        record.setResourceType(normalize(request.resourceType()).toUpperCase(Locale.ROOT));
        record.setResourceId(request.resourceId());
        record.setMaintenanceType(normalize(request.maintenanceType()));
        record.setDescription(normalize(request.description()));
        record.setScheduledDate(request.scheduledDate());
        record.setStartDate(request.startDate());
        record.setCompletionDate(request.completionDate());
        record.setStatus(status);
        record.setServiceNotes(normalize(request.serviceNotes()));
        record.setTechnician(normalize(request.technician()));
        applyStatusDates(record, status);
        validateDateOrder(record.getStartDate(), record.getCompletionDate());
        return toResponse(maintenanceRepository.save(record));
    }

    @Transactional(readOnly = true)
    public List<MaintenanceResponse> getAll(String resourceType, Long resourceId, String status) {
        if (resourceId != null && resourceId <= 0) {
            throw badRequest("Resource ID must be greater than zero");
        }
        if (!isBlank(status)) {
            validateStatus(normalizeStatus(status));
        }

        Specification<MaintenanceRecord> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (!isBlank(resourceType)) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("resourceType")),
                        normalize(resourceType).toLowerCase(Locale.ROOT)));
            }
            if (resourceId != null) {
                predicates.add(criteriaBuilder.equal(root.get("resourceId"), resourceId));
            }
            if (!isBlank(status)) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.upper(root.get("status")),
                        normalizeStatus(status)));
            }
            query.orderBy(criteriaBuilder.asc(root.get("scheduledDate")));
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return maintenanceRepository.findAll(specification).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public MaintenanceResponse getById(Long id) {
        return toResponse(findRecord(id));
    }

    public MaintenanceResponse update(Long id, MaintenanceUpdateRequest request) {
        if (request == null) {
            throw badRequest("Maintenance request is required");
        }
        validateFields(request.resourceType(), request.resourceId(), request.maintenanceType(),
                request.description(), request.scheduledDate(), request.startDate(), request.completionDate());
        String nextStatus = normalizeStatus(request.status());
        validateStatus(nextStatus);

        MaintenanceRecord record = findRecord(id);
        validateStatusTransition(record.getStatus(), nextStatus);
        record.setResourceType(normalize(request.resourceType()).toUpperCase(Locale.ROOT));
        record.setResourceId(request.resourceId());
        record.setMaintenanceType(normalize(request.maintenanceType()));
        record.setDescription(normalize(request.description()));
        record.setScheduledDate(request.scheduledDate());
        record.setStartDate(request.startDate());
        record.setCompletionDate(request.completionDate());
        record.setStatus(nextStatus);
        record.setServiceNotes(normalize(request.serviceNotes()));
        record.setTechnician(normalize(request.technician()));
        applyStatusDates(record, nextStatus);
        validateDateOrder(record.getStartDate(), record.getCompletionDate());
        return toResponse(maintenanceRepository.save(record));
    }

    public MaintenanceResponse updateStatus(Long id, MaintenanceStatusRequest request) {
        if (request == null || isBlank(request.status())) {
            throw badRequest("Status is required");
        }
        String nextStatus = normalizeStatus(request.status());
        validateStatus(nextStatus);

        MaintenanceRecord record = findRecord(id);
        validateStatusTransition(record.getStatus(), nextStatus);
        record.setStatus(nextStatus);
        applyStatusDates(record, nextStatus);
        validateDateOrder(record.getStartDate(), record.getCompletionDate());
        return toResponse(maintenanceRepository.save(record));
    }

    public MaintenanceResponse updateNotes(Long id, MaintenanceNotesRequest request) {
        if (request == null || isBlank(request.serviceNotes())) {
            throw badRequest("Service notes are required");
        }
        MaintenanceRecord record = findRecord(id);
        record.setServiceNotes(normalize(request.serviceNotes()));
        return toResponse(maintenanceRepository.save(record));
    }

    public void delete(Long id) {
        maintenanceRepository.delete(findRecord(id));
    }

    @Transactional(readOnly = true)
    public List<MaintenanceResponse> getUpcoming() {
        return maintenanceRepository.findUpcomingScheduled(LocalDateTime.now())
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<MaintenanceResponse> getOverdue() {
        return maintenanceRepository.findOverdueScheduled(LocalDateTime.now())
                .stream().map(this::toResponse).toList();
    }

    private MaintenanceRecord findRecord(Long id) {
        return maintenanceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Maintenance record not found with ID: " + id));
    }

    private void validateFields(String resourceType, Long resourceId, String maintenanceType, String description,
                                LocalDateTime scheduledDate, LocalDateTime startDate, LocalDateTime completionDate) {
        requireText(resourceType, "Resource type");
        if (resourceId == null || resourceId <= 0) {
            throw badRequest("Resource ID must be greater than zero");
        }
        requireText(maintenanceType, "Maintenance type");
        requireText(description, "Description");
        if (scheduledDate == null) {
            throw badRequest("Scheduled date is required");
        }
        validateDateOrder(startDate, completionDate);
    }

    private void validateDateOrder(LocalDateTime startDate, LocalDateTime completionDate) {
        if (startDate != null && completionDate != null && completionDate.isBefore(startDate)) {
            throw badRequest("Completion date cannot be earlier than start date");
        }
    }

    private void validateStatus(String status) {
        if (status == null || !status.matches(VALID_STATUS_PATTERN)) {
            throw badRequest("Invalid maintenance status");
        }
    }

    private void validateStatusTransition(String currentStatus, String nextStatus) {
        if ("COMPLETED".equals(currentStatus) && "IN_PROGRESS".equals(nextStatus)) {
            throw badRequest("A completed maintenance record cannot be moved back to in progress");
        }
    }

    private void applyStatusDates(MaintenanceRecord record, String status) {
        if ("IN_PROGRESS".equals(status) && record.getStartDate() == null) {
            record.setStartDate(LocalDateTime.now());
        }
        if ("COMPLETED".equals(status) && record.getCompletionDate() == null) {
            record.setCompletionDate(LocalDateTime.now());
        }
    }

    private void requireText(String value, String label) {
        if (isBlank(value)) {
            throw badRequest(label + " is required");
        }
    }

    private ResponseStatusException badRequest(String reason) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, reason);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private String normalizeStatus(String status) {
        return status == null ? null : status.trim().toUpperCase(Locale.ROOT);
    }

    private MaintenanceResponse toResponse(MaintenanceRecord record) {
        return new MaintenanceResponse(record.getMaintenanceId(), record.getResourceType(), record.getResourceId(),
                record.getMaintenanceType(), record.getDescription(), record.getScheduledDate(), record.getStartDate(),
                record.getCompletionDate(), record.getStatus(), record.getServiceNotes(), record.getTechnician(),
                record.getCreatedAt(), record.getUpdatedAt());
    }
}