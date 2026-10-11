package com.team11.logistics.service;

import com.team11.logistics.dto.CreateLogisticsRequestDto;
import com.team11.logistics.dto.LogisticsRequestResponse;
import com.team11.logistics.dto.RequestItemDto;
import com.team11.logistics.dto.UpdateLogisticsRequestDto;
import com.team11.logistics.entity.LogisticsRequest;
import com.team11.logistics.entity.RequestLineItem;
import com.team11.logistics.model.RequestPriority;
import com.team11.logistics.model.RequestStatus;
import com.team11.logistics.repository.LogisticsRequestRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class LogisticsRequestService {

    private final LogisticsRequestRepository logisticsRequestRepository;

    public LogisticsRequestService(LogisticsRequestRepository logisticsRequestRepository) {
        this.logisticsRequestRepository = logisticsRequestRepository;
    }

    @Transactional
    public LogisticsRequestResponse createDraft(CreateLogisticsRequestDto dto) {
        LogisticsRequest request = new LogisticsRequest();
        request.setRequestNumber(generateRequestNumber());
        request.setRequestingUnit(normalize(dto.getRequestingUnit()));
        request.setRequesterName(normalizeOptional(dto.getRequesterName()));
        request.setPurpose(normalize(dto.getPurpose()));
        request.setPriority(dto.getPriority());
        request.setRequiredFrom(dto.getRequiredFrom());
        request.setRequiredUntil(dto.getRequiredUntil());
        request.setStatus(RequestStatus.DRAFT);

        List<RequestLineItem> items = new ArrayList<>();
        for (RequestItemDto itemDto : dto.getItems()) {
            items.add(toLineItem(itemDto));
        }
        request.setItems(items);

        LogisticsRequest saved = logisticsRequestRepository.save(request);
        return toResponse(saved);
    }

    @Transactional
    public LogisticsRequestResponse updateDraft(Long id, UpdateLogisticsRequestDto dto) {
        LogisticsRequest request = getRequestEntity(id);
        ensureDraftEditable(request);

        request.setRequestingUnit(normalize(dto.getRequestingUnit()));
        request.setRequesterName(normalizeOptional(dto.getRequesterName()));
        request.setPurpose(normalize(dto.getPurpose()));
        request.setPriority(dto.getPriority());
        request.setRequiredFrom(dto.getRequiredFrom());
        request.setRequiredUntil(dto.getRequiredUntil());

        request.getItems().clear();
        List<RequestLineItem> updatedItems = new ArrayList<>();
        for (RequestItemDto itemDto : dto.getItems()) {
            RequestLineItem item = toLineItem(itemDto);
            item.setRequest(request);
            updatedItems.add(item);
        }
        request.getItems().addAll(updatedItems);

        LogisticsRequest saved = logisticsRequestRepository.save(request);
        return toResponse(saved);
    }

    @Transactional
    public LogisticsRequestResponse submitRequest(Long id) {
        LogisticsRequest request = getRequestEntity(id);
        if (request.getStatus() != RequestStatus.DRAFT) {
            throw new IllegalStateException("Only draft requests can be submitted");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalStateException("Request must contain at least one line item before submission");
        }

        request.setStatus(RequestStatus.SUBMITTED);
        return toResponse(logisticsRequestRepository.save(request));
    }

    @Transactional
    public LogisticsRequestResponse cancelRequest(Long id) {
        LogisticsRequest request = getRequestEntity(id);
        if (request.getStatus() != RequestStatus.DRAFT && request.getStatus() != RequestStatus.SUBMITTED) {
            throw new IllegalStateException("Only draft or submitted requests can be cancelled");
        }

        request.setStatus(RequestStatus.CANCELLED);
        return toResponse(logisticsRequestRepository.save(request));
    }

    @Transactional(readOnly = true)
    public LogisticsRequestResponse getRequest(Long id) {
        return toResponse(getRequestEntity(id));
    }

    @Transactional(readOnly = true)
    public List<LogisticsRequestResponse> listRequests(
            RequestStatus status,
            String requestingUnit,
            RequestPriority priority,
            LocalDate createdFrom,
            LocalDate createdTo) {

        if (createdFrom != null && createdTo != null && createdTo.isBefore(createdFrom)) {
            throw new IllegalArgumentException("Created to must not be earlier than created from");
        }

        Specification<LogisticsRequest> spec = Specification.where((Specification<LogisticsRequest>) null);

        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (requestingUnit != null && !requestingUnit.isBlank()) {
            String value = "%" + requestingUnit.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("requestingUnit")), value));
        }
        if (priority != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("priority"), priority));
        }
        if (createdFrom != null || createdTo != null) {
            LocalDateTime start = createdFrom != null ? createdFrom.atStartOfDay() : LocalDateTime.of(1970, 1, 1, 0, 0);
            LocalDateTime end = createdTo != null ? createdTo.plusDays(1).atStartOfDay() : LocalDateTime.now().plusYears(100);
            spec = spec.and((root, query, cb) -> cb.between(root.get("createdAt"), start, end));
        }

        return logisticsRequestRepository.findAll(spec)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private LogisticsRequest getRequestEntity(Long id) {
        return logisticsRequestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Logistics request not found with ID: " + id));
    }

    private void ensureDraftEditable(LogisticsRequest request) {
        if (request.getStatus() != RequestStatus.DRAFT) {
            throw new IllegalStateException("Only draft requests can be edited");
        }
    }

    private RequestLineItem toLineItem(RequestItemDto dto) {
        RequestLineItem item = new RequestLineItem();
        item.setResourceType(dto.getResourceType());
        item.setResourceReference(normalize(dto.getResourceReference()));
        item.setQuantity(dto.getQuantity());
        item.setNotes(normalizeOptional(dto.getNotes()));
        item.setFulfilledQuantity(0);
        return item;
    }

    private LogisticsRequestResponse toResponse(LogisticsRequest request) {
        LogisticsRequestResponse response = new LogisticsRequestResponse();
        response.setId(request.getId());
        response.setRequestNumber(request.getRequestNumber());
        response.setRequestingUnit(request.getRequestingUnit());
        response.setRequesterName(request.getRequesterName());
        response.setPurpose(request.getPurpose());
        response.setPriority(request.getPriority());
        response.setRequiredFrom(request.getRequiredFrom());
        response.setRequiredUntil(request.getRequiredUntil());
        response.setStatus(request.getStatus());
        response.setCreatedAt(request.getCreatedAt());
        response.setUpdatedAt(request.getUpdatedAt());

        List<LogisticsRequestResponse.RequestLineItemResponse> itemResponses = new ArrayList<>();
        if (request.getItems() != null) {
            for (RequestLineItem item : request.getItems()) {
                LogisticsRequestResponse.RequestLineItemResponse itemResponse = new LogisticsRequestResponse.RequestLineItemResponse();
                itemResponse.setId(item.getId());
                itemResponse.setResourceType(item.getResourceType());
                itemResponse.setResourceReference(item.getResourceReference());
                itemResponse.setQuantity(item.getQuantity());
                itemResponse.setNotes(item.getNotes());
                itemResponse.setFulfilledQuantity(item.getFulfilledQuantity());
                itemResponses.add(itemResponse);
            }
        }
        response.setItems(itemResponses);
        return response;
    }

    private String generateRequestNumber() {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        return "LR-" + suffix;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        return value.trim();
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
