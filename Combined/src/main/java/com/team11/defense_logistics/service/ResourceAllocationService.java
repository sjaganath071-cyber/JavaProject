package com.team11.defense_logistics.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.team11.defense_logistics.model.ResourceAllocation;
import com.team11.logistics.dto.AllocationResponse;
import com.team11.logistics.dto.CreateAllocationDto;
import com.team11.logistics.model.AllocationStatus;

@Service
public class ResourceAllocationService {

    private final RestClient restClient;

    public ResourceAllocationService(
            RestClient.Builder restClientBuilder,
            @Value("${services.logistics-allocations.base-url:http://localhost:8082/api/v1/allocations}")
            String baseUrl) {
        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .build();
    }

    public List<ResourceAllocation> findAll() {
        try {
            AllocationResponse[] response = restClient.get()
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(AllocationResponse[].class);

            if (response == null) {
                return List.of();
            }
            return Arrays.stream(response)
                    .map(this::toFrontendModel)
                    .toList();

        } catch (RestClientException ex) {
            throw new IllegalStateException(
                    "Unable to retrieve resource allocations from the backend.",
                    ex);
        }
    }

    public Optional<ResourceAllocation> findById(Long id) {
        try {
            AllocationResponse response = restClient.get()
                    .uri("/{id}", id)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(AllocationResponse.class);
            return response == null ? Optional.empty() : Optional.of(toFrontendModel(response));

        } catch (RestClientException ex) {
            throw new IllegalStateException(
                    "Unable to retrieve allocation with ID " + id + " from the backend.",
                    ex);
        }
    }

    public List<ResourceAllocation> byRequest(Long requestId) {
        try {
            AllocationResponse[] response = restClient.get()
                    .uri(uriBuilder -> uriBuilder.queryParam("requestId", requestId).build())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(AllocationResponse[].class);

            if (response == null) {
                return List.of();
            }
            return Arrays.stream(response)
                    .map(this::toFrontendModel)
                    .toList();

        } catch (RestClientException ex) {
            throw new IllegalStateException(
                    "Unable to retrieve allocations for request " + requestId + ".",
                    ex);
        }
    }

    public List<ResourceAllocation> byVehicle(Long vehicleId) {
        return findAll().stream()
                .filter(allocation -> allocation.getVehicleId() != null
                        && allocation.getVehicleId().equals(vehicleId))
                .toList();
    }

    public List<ResourceAllocation> byStatus(String status) {
        String normalizedStatus = status == null ? null : status.trim();
        if (normalizedStatus == null || normalizedStatus.isBlank()) {
            return findAll();
        }
        return findAll().stream()
                .filter(allocation -> normalizedStatus.equalsIgnoreCase(allocation.getStatus()))
                .toList();
    }

    public ResourceAllocation create(ResourceAllocation allocation) {
        if (allocation == null) {
            throw new IllegalArgumentException("Allocation payload is required.");
        }
        if (allocation.getRequestId() == null || allocation.getRequestId() <= 0) {
            throw new IllegalArgumentException("Request ID is required.");
        }
        if (allocation.getRequestLineItemId() == null || allocation.getRequestLineItemId() <= 0) {
            throw new IllegalArgumentException("Request line item ID is required.");
        }
        if (allocation.getQuantity() == null || allocation.getQuantity() < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1.");
        }

        CreateAllocationDto payload = new CreateAllocationDto();
        payload.setRequestLineItemId(allocation.getRequestLineItemId());
        payload.setQuantity(allocation.getQuantity());

        try {
            AllocationResponse response = restClient.post()
                    .uri("/requests/{requestId}", allocation.getRequestId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(AllocationResponse.class);
            if (response == null) {
                throw new IllegalStateException("The backend returned no allocation record.");
            }
            return toFrontendModel(response);
        } catch (RestClientException ex) {
            throw new IllegalStateException(
                    "Unable to create the resource allocation in the backend. Verify the selected request line item and quantity.",
                    ex);
        }
    }

    public ResourceAllocation release(Long id) {
        try {
            AllocationResponse response = restClient.post()
                    .uri("/{id}/release", id)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(AllocationResponse.class);
            return response == null ? null : toFrontendModel(response);

        } catch (RestClientException ex) {
            throw new IllegalStateException(
                    "Unable to release allocation with ID " + id + " via the backend.",
                    ex);
        }
    }

    public ResourceAllocation cancel(Long id) {
        throw new IllegalStateException(
                "Allocation cancellation is not supported by the current Jagan API contract.");
    }

    private ResourceAllocation toFrontendModel(AllocationResponse response) {
        ResourceAllocation allocation = new ResourceAllocation();
        allocation.setId(response.getId());
        allocation.setRequestId(response.getRequestId());
        allocation.setRequestLineItemId(response.getRequestLineItemId());
        allocation.setQuantity(response.getQuantity());
        allocation.setVehicleId(null);
        allocation.setResourceName(resolveResourceName(response));
        allocation.setAllocatedTo(resolveAllocatedTo(response));
        allocation.setStatus(resolveFrontendStatus(response.getStatus()));
        allocation.setAllocationDate(response.getAllocatedAt());
        allocation.setReleaseDate(response.getReleasedAt());
        return allocation;
    }

    private String resolveResourceName(AllocationResponse response) {
        if (response.getResourceReference() != null && !response.getResourceReference().isBlank()) {
            return response.getResourceReference();
        }
        if (response.getResourceType() != null) {
            return response.getResourceType().name();
        }
        return "Resource";
    }

    private String resolveAllocatedTo(AllocationResponse response) {
        if (response.getRequestNumber() != null && !response.getRequestNumber().isBlank()) {
            return response.getRequestNumber();
        }
        if (response.getRequestId() != null) {
            return "Request " + response.getRequestId();
        }
        return "Unassigned";
    }

    private String resolveFrontendStatus(AllocationStatus status) {
        if (status == null) {
            return "Allocated";
        }
        return switch (status) {
            case ALLOCATED -> "Allocated";
            case RELEASED -> "Released";
            case COMPLETED -> "Completed";
        };
    }
}
