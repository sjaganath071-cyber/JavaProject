package com.team11.logistics.service;

import com.team11.logistics.client.ResourceServiceClient;
import com.team11.logistics.dto.AllocationResponse;
import com.team11.logistics.dto.CreateAllocationDto;
import com.team11.logistics.dto.ResourceAvailabilityResponse;
import com.team11.logistics.dto.ResourceReservationRequest;
import com.team11.logistics.dto.ResourceReservationResponse;
import com.team11.logistics.entity.LogisticsRequest;
import com.team11.logistics.entity.RequestLineItem;
import com.team11.logistics.entity.ResourceAllocation;
import com.team11.logistics.model.AllocationStatus;
import com.team11.logistics.repository.LogisticsRequestRepository;
import com.team11.logistics.repository.ResourceAllocationRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ResourceAllocationService {

    private final LogisticsRequestRepository logisticsRequestRepository;
    private final ResourceAllocationRepository resourceAllocationRepository;
    private final ResourceServiceClient resourceServiceClient;

    public ResourceAllocationService(
            LogisticsRequestRepository logisticsRequestRepository,
            ResourceAllocationRepository resourceAllocationRepository,
            ResourceServiceClient resourceServiceClient) {
        this.logisticsRequestRepository = logisticsRequestRepository;
        this.resourceAllocationRepository = resourceAllocationRepository;
        this.resourceServiceClient = resourceServiceClient;
    }

    @Transactional
    public AllocationResponse createAllocation(Long requestId, CreateAllocationDto dto) {
        LogisticsRequest request = logisticsRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Logistics request not found with ID: " + requestId));

        if (!isApprovedRequest(request)) {
            throw new IllegalStateException("Only approved requests can receive allocations");
        }

        RequestLineItem lineItem = request.getItems().stream()
                .filter(item -> item.getId() != null && item.getId().equals(dto.getRequestLineItemId()))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException(
                        "Request line item not found with ID: " + dto.getRequestLineItemId()));

        if (!lineItem.getRequest().getId().equals(requestId)) {
            throw new IllegalStateException("Request line item does not belong to the supplied request");
        }

        if (dto.getQuantity() == null || dto.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        int activeAllocatedQuantity = resourceAllocationRepository
                .findByRequestLineItemIdOrderByAllocatedAtDesc(lineItem.getId())
                .stream()
                .filter(allocation -> allocation.getStatus() == AllocationStatus.ALLOCATED
                        || allocation.getStatus() == AllocationStatus.COMPLETED)
                .mapToInt(ResourceAllocation::getQuantity)
                .sum();

        int remainingUnallocated = lineItem.getQuantity() - lineItem.getFulfilledQuantity() - activeAllocatedQuantity;
        if (dto.getQuantity() > remainingUnallocated) {
            throw new IllegalStateException("Requested quantity exceeds the remaining unallocated quantity");
        }

        if (!resourceServiceClient.isConfigured()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Resource Service contract is not configured. Kaviya must confirm the availability and reservation endpoints.");
        }

        ResourceAvailabilityResponse availability = resourceServiceClient.checkAvailability(
                lineItem.getResourceType(),
                lineItem.getResourceReference(),
                dto.getQuantity());

        if (availability == null || !availability.isAvailable()) {
            throw new IllegalStateException(
                    availability != null ? availability.getMessage() : "Resource availability is insufficient");
        }

        ResourceReservationRequest reservationRequest = new ResourceReservationRequest();
        reservationRequest.setResourceType(lineItem.getResourceType().name());
        reservationRequest.setResourceReference(lineItem.getResourceReference());
        reservationRequest.setQuantity(dto.getQuantity());
        reservationRequest.setRequestId(request.getRequestNumber());
        reservationRequest.setCorrelationId("allocation-" + requestId + "-" + lineItem.getId());

        ResourceReservationResponse reservationResponse = resourceServiceClient.reserveResource(reservationRequest);
        if (reservationResponse == null || !reservationResponse.isSuccess()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    reservationResponse != null ? reservationResponse.getMessage() : "Resource reservation outcome is unknown. The allocation was not persisted.");
        }

        ResourceAllocation allocation = new ResourceAllocation();
        allocation.setRequest(request);
        allocation.setRequestLineItem(lineItem);
        allocation.setResourceType(lineItem.getResourceType());
        allocation.setResourceReference(lineItem.getResourceReference());
        allocation.setQuantity(dto.getQuantity());
        allocation.setStatus(AllocationStatus.ALLOCATED);
        allocation.setReservationReference(reservationResponse.getReservationReference());

        ResourceAllocation saved = resourceAllocationRepository.save(allocation);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<AllocationResponse> listAllocations(Long requestId, AllocationStatus status) {
        List<ResourceAllocation> allocations;
        if (requestId != null && status != null) {
            allocations = resourceAllocationRepository.findByRequestIdAndStatusOrderByAllocatedAtDesc(requestId, status);
        } else if (requestId != null) {
            allocations = resourceAllocationRepository.findByRequestIdOrderByAllocatedAtDesc(requestId);
        } else if (status != null) {
            allocations = resourceAllocationRepository.findByStatusOrderByAllocatedAtDesc(status);
        } else {
            allocations = resourceAllocationRepository.findAllByOrderByAllocatedAtDesc();
        }

        return allocations.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public AllocationResponse getAllocation(Long allocationId) {
        return toResponse(getAllocationEntity(allocationId));
    }

    @Transactional
    public AllocationResponse releaseAllocation(Long allocationId) {
        ResourceAllocation allocation = getAllocationEntity(allocationId);
        if (allocation.getStatus() != AllocationStatus.ALLOCATED) {
            throw new IllegalStateException("Only allocated resources can be released");
        }

        // No confirmed remote release API exists in the current project contract.
        allocation.setStatus(AllocationStatus.RELEASED);
        allocation.setReleasedAt(LocalDateTime.now());
        return toResponse(resourceAllocationRepository.save(allocation));
    }

    @Transactional
    public AllocationResponse completeAllocation(Long allocationId) {
        ResourceAllocation allocation = getAllocationEntity(allocationId);
        if (allocation.getStatus() != AllocationStatus.ALLOCATED) {
            throw new IllegalStateException("Only allocated resources can be completed");
        }

        allocation.setStatus(AllocationStatus.COMPLETED);
        allocation.setCompletedAt(LocalDateTime.now());
        return toResponse(resourceAllocationRepository.save(allocation));
    }

    private ResourceAllocation getAllocationEntity(Long allocationId) {
        return resourceAllocationRepository.findById(allocationId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Resource allocation not found with ID: " + allocationId));
    }

    private boolean isApprovedRequest(LogisticsRequest request) {
        if (request == null || request.getStatus() == null) {
            return false;
        }

        String statusName = request.getStatus().name();
        return "APPROVED".equalsIgnoreCase(statusName)
                || "SUBMITTED".equalsIgnoreCase(statusName);
    }

    private AllocationResponse toResponse(ResourceAllocation allocation) {
        AllocationResponse response = new AllocationResponse();
        response.setId(allocation.getId());
        response.setRequestId(allocation.getRequest() != null ? allocation.getRequest().getId() : null);
        response.setRequestNumber(allocation.getRequest() != null ? allocation.getRequest().getRequestNumber() : null);
        response.setRequestLineItemId(allocation.getRequestLineItem() != null ? allocation.getRequestLineItem().getId() : null);
        response.setResourceType(allocation.getResourceType());
        response.setResourceReference(allocation.getResourceReference());
        response.setQuantity(allocation.getQuantity());
        response.setStatus(allocation.getStatus());
        response.setAllocatedAt(allocation.getAllocatedAt());
        response.setReleasedAt(allocation.getReleasedAt());
        response.setCompletedAt(allocation.getCompletedAt());
        response.setReservationReference(allocation.getReservationReference());
        return response;
    }
}
