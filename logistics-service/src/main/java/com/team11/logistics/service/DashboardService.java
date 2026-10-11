package com.team11.logistics.service;

import com.team11.logistics.client.ResourceServiceClient;
import com.team11.logistics.dto.DashboardSummaryResponse;
import com.team11.logistics.model.AllocationStatus;
import com.team11.logistics.model.RequestStatus;
import com.team11.logistics.repository.LogisticsRequestRepository;
import com.team11.logistics.repository.ResourceAllocationRepository;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    private final LogisticsRequestRepository logisticsRequestRepository;
    private final ResourceAllocationRepository resourceAllocationRepository;
    private final ResourceServiceClient resourceServiceClient;

    public DashboardService(
            LogisticsRequestRepository logisticsRequestRepository,
            ResourceAllocationRepository resourceAllocationRepository,
            ResourceServiceClient resourceServiceClient) {
        this.logisticsRequestRepository = logisticsRequestRepository;
        this.resourceAllocationRepository = resourceAllocationRepository;
        this.resourceServiceClient = resourceServiceClient;
    }

    public DashboardSummaryResponse getSummary() {
        DashboardSummaryResponse response = new DashboardSummaryResponse();

        response.setTotalRequests(logisticsRequestRepository.count());
        response.setPendingRequests(countByStatus("DRAFT"));
        response.setAwaitingApproval(countByStatus("SUBMITTED"));
        response.setApprovedUnallocatedRequests(countApprovedUnallocatedRequests());
        response.setActiveAllocations(resourceAllocationRepository.countByStatus(AllocationStatus.ALLOCATED));
        response.setActiveMovements(0L);
        response.setCompletedRequests(countByStatus("COMPLETED"));
        response.setCancelledRequests(countByStatus("CANCELLED"));

        if (resourceServiceClient == null || !resourceServiceClient.isConfigured()) {
            response.setTotalResources(null);
            response.setLowStockResources(null);
            response.setResourceServiceAvailable(false);
            response.setResourceServiceMessage("Resource Service summary endpoint is not configured");
            return response;
        }

        response.setTotalResources(null);
        response.setLowStockResources(null);
        response.setResourceServiceAvailable(false);
        response.setResourceServiceMessage("Resource Service summary endpoint contract is not available in the current project");
        return response;
    }

    private long countByStatus(String statusName) {
        if (!hasEnumValue(RequestStatus.class, statusName)) {
            return 0L;
        }
        return logisticsRequestRepository.countByStatus(RequestStatus.valueOf(statusName));
    }

    private long countApprovedUnallocatedRequests() {
        if (!hasEnumValue(RequestStatus.class, "APPROVED")) {
            return 0L;
        }

        RequestStatus approved = RequestStatus.valueOf("APPROVED");
        return logisticsRequestRepository.countApprovedRequestsWithoutActiveAllocation(approved, AllocationStatus.ALLOCATED);
    }

    private <E extends Enum<E>> boolean hasEnumValue(Class<E> enumClass, String statusName) {
        try {
            Enum.valueOf(enumClass, statusName);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
