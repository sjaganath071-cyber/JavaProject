package com.team11.defense_logistics.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.team11.defense_logistics.dto.LogisticsRequestDto;
import com.team11.logistics.dto.CreateLogisticsRequestDto;
import com.team11.logistics.dto.LogisticsRequestResponse;
import com.team11.logistics.dto.RequestItemDto;
import com.team11.logistics.dto.UpdateLogisticsRequestDto;
import com.team11.logistics.model.RequestPriority;
import com.team11.logistics.model.RequestStatus;
import com.team11.logistics.model.ResourceType;

@Service
public class LogisticsRequestService {

    private final RestClient restClient;

    public LogisticsRequestService(
            RestClient.Builder restClientBuilder,
            @Value("${services.logistics-requests.base-url:http://localhost:8082/api/v1/requests}")
            String baseUrl) {

        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .build();
    }

    public List<LogisticsRequestDto> getAllRequests() {
        try {
            LogisticsRequestResponse[] response = restClient.get()
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(LogisticsRequestResponse[].class);

            if (response == null) {
                return Collections.emptyList();
            }

            List<LogisticsRequestDto> requests = new ArrayList<>();
            for (LogisticsRequestResponse item : response) {
                requests.add(toFrontendDto(item));
            }
            return requests;

        } catch (RestClientException ex) {
            throw new IllegalStateException(
                    "Unable to retrieve logistics requests from the backend.",
                    ex);
        }
    }

    public LogisticsRequestDto getRequestById(Long id) {
        try {
            LogisticsRequestResponse response = restClient.get()
                    .uri("/{id}", id)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(LogisticsRequestResponse.class);
            return response == null ? null : toFrontendDto(response);

        } catch (RestClientException ex) {
            throw new IllegalStateException(
                    "Unable to retrieve logistics request with ID " + id
                            + " from the backend.",
                    ex);
        }
    }

    public List<RequestLineItemOption> getApprovedRequestLineItems() {
        try {
            LogisticsRequestResponse[] response = restClient.get()
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(LogisticsRequestResponse[].class);

            if (response == null) {
                return Collections.emptyList();
            }

            List<RequestLineItemOption> items = new ArrayList<>();
            for (LogisticsRequestResponse request : response) {
                if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
                    continue;
                }
                if (!"APPROVED".equalsIgnoreCase(resolveFrontendStatus(request.getStatus()))) {
                    continue;
                }
                for (LogisticsRequestResponse.RequestLineItemResponse item : request.getItems()) {
                    if (item == null || item.getId() == null) {
                        continue;
                    }
                    items.add(new RequestLineItemOption(
                            request.getId(),
                            request.getRequestNumber(),
                            item.getId(),
                            item.getResourceType(),
                            item.getResourceReference(),
                            item.getQuantity(),
                            item.getFulfilledQuantity()
                    ));
                }
            }
            return items;

        } catch (RestClientException ex) {
            throw new IllegalStateException(
                    "Unable to retrieve approved request line items from the backend.",
                    ex);
        }
    }

    public Long getRequestIdForLineItem(Long requestLineItemId) {
        if (requestLineItemId == null || requestLineItemId <= 0) {
            return null;
        }
        for (RequestLineItemOption item : getApprovedRequestLineItems()) {
            if (Objects.equals(item.getRequestLineItemId(), requestLineItemId)) {
                return item.getRequestId();
            }
        }
        return null;
    }

    public LogisticsRequestDto createRequest(LogisticsRequestDto request) {
        try {
            CreateLogisticsRequestDto payload = toCreateRequestDto(request);
            LogisticsRequestResponse response = restClient.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(LogisticsRequestResponse.class);
            return response == null ? null : toFrontendDto(response);

        } catch (RestClientException ex) {
            throw new IllegalStateException(
                    "Unable to create logistics request in the backend.",
                    ex);
        }
    }

    public LogisticsRequestDto updateRequest(Long id, LogisticsRequestDto request) {
        try {
            UpdateLogisticsRequestDto payload = toUpdateRequestDto(request);
            LogisticsRequestResponse response = restClient.put()
                    .uri("/{id}", id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(LogisticsRequestResponse.class);
            return response == null ? null : toFrontendDto(response);

        } catch (RestClientException ex) {
            throw new IllegalStateException(
                    "Unable to update logistics request in the backend.",
                    ex);
        }
    }

    private LogisticsRequestDto toFrontendDto(LogisticsRequestResponse response) {
        LogisticsRequestDto dto = new LogisticsRequestDto();
        dto.setId(response.getId());
        dto.setRequestCode(response.getRequestNumber());
        dto.setRequestingUnit(response.getRequestingUnit());
        dto.setRequestType(resolveRequestType(response));
        dto.setDescription(response.getPurpose());
        dto.setPriority(resolveFrontendPriority(response.getPriority()));
        dto.setStatus(resolveFrontendStatus(response.getStatus()));
        dto.setRequestedDate(response.getRequiredFrom() != null ? response.getRequiredFrom() : LocalDate.now());
        dto.setRequiredByDate(response.getRequiredUntil());
        dto.setAssignedResource(resolveAssignedResource(response));
        dto.setNotes(resolveNotes(response));
        return dto;
    }

    private CreateLogisticsRequestDto toCreateRequestDto(LogisticsRequestDto request) {
        CreateLogisticsRequestDto dto = new CreateLogisticsRequestDto();
        dto.setRequestingUnit(request.getRequestingUnit());
        dto.setRequesterName(null);
        dto.setPurpose(request.getDescription());
        dto.setPriority(parsePriority(request.getPriority()));
        dto.setRequiredFrom(request.getRequestedDate());
        dto.setRequiredUntil(request.getRequiredByDate());
        dto.setItems(buildRequestItems(request));
        return dto;
    }

    private UpdateLogisticsRequestDto toUpdateRequestDto(LogisticsRequestDto request) {
        UpdateLogisticsRequestDto dto = new UpdateLogisticsRequestDto();
        dto.setRequestingUnit(request.getRequestingUnit());
        dto.setRequesterName(null);
        dto.setPurpose(request.getDescription());
        dto.setPriority(parsePriority(request.getPriority()));
        dto.setRequiredFrom(request.getRequestedDate());
        dto.setRequiredUntil(request.getRequiredByDate());
        dto.setItems(buildRequestItems(request));
        return dto;
    }

    private List<RequestItemDto> buildRequestItems(LogisticsRequestDto request) {
        ResourceType resourceType = resolveResourceType(request.getRequestType());
        String resourceReference = request.getAssignedResource() != null && !request.getAssignedResource().isBlank()
                ? request.getAssignedResource() : request.getRequestCode() != null ? request.getRequestCode() : "UNSPECIFIED";

        RequestItemDto item = new RequestItemDto();
        item.setResourceType(resourceType);
        item.setResourceReference(resourceReference);
        item.setQuantity(1);
        item.setNotes(request.getNotes());
        return List.of(item);
    }

    private String resolveRequestType(LogisticsRequestResponse response) {
        if (response.getItems() == null || response.getItems().isEmpty()) {
            return "OTHER";
        }

        ResourceType type = response.getItems().get(0).getResourceType();
        if (type == null) {
            return "OTHER";
        }
        return switch (type) {
            case VEHICLE -> "VEHICLE";
            case ASSET -> "EQUIPMENT";
            case PERSONNEL -> "PERSONNEL";
            case SUPPLY -> "SUPPLIES";
        };
    }

    private String resolveAssignedResource(LogisticsRequestResponse response) {
        if (response.getItems() == null || response.getItems().isEmpty()) {
            return null;
        }

        List<String> refs = new ArrayList<>();
        for (LogisticsRequestResponse.RequestLineItemResponse item : response.getItems()) {
            if (item.getResourceReference() != null && !item.getResourceReference().isBlank()) {
                refs.add(item.getResourceReference());
            }
        }
        return refs.isEmpty() ? null : String.join(", ", refs);
    }

    private String resolveNotes(LogisticsRequestResponse response) {
        if (response.getItems() == null || response.getItems().isEmpty()) {
            return null;
        }

        List<String> notes = new ArrayList<>();
        for (LogisticsRequestResponse.RequestLineItemResponse item : response.getItems()) {
            if (item.getNotes() != null && !item.getNotes().isBlank()) {
                notes.add(item.getNotes());
            }
        }
        return notes.isEmpty() ? null : String.join("; ", notes);
    }

    private String resolveFrontendStatus(RequestStatus status) {
        if (status == null) {
            return "PENDING";
        }
        return switch (status) {
            case DRAFT -> "PENDING";
            case SUBMITTED -> "APPROVED";
            case CANCELLED -> "REJECTED";
        };
    }

    private String resolveFrontendPriority(RequestPriority priority) {
        if (priority == null) {
            return "MEDIUM";
        }
        return switch (priority) {
            case LOW -> "LOW";
            case NORMAL -> "MEDIUM";
            case HIGH -> "HIGH";
            case URGENT -> "CRITICAL";
        };
    }

    private RequestPriority parsePriority(String priority) {
        if (priority == null || priority.isBlank()) {
            return RequestPriority.NORMAL;
        }
        return switch (priority.trim().toUpperCase()) {
            case "LOW" -> RequestPriority.LOW;
            case "MEDIUM" -> RequestPriority.NORMAL;
            case "HIGH" -> RequestPriority.HIGH;
            case "CRITICAL" -> RequestPriority.URGENT;
            default -> RequestPriority.NORMAL;
        };
    }

    private ResourceType resolveResourceType(String requestType) {
        if (requestType == null || requestType.isBlank()) {
            return ResourceType.SUPPLY;
        }
        return switch (requestType.trim().toUpperCase()) {
            case "PERSONNEL" -> ResourceType.PERSONNEL;
            case "VEHICLE" -> ResourceType.VEHICLE;
            case "EQUIPMENT" -> ResourceType.ASSET;
            case "SUPPLIES", "OTHER" -> ResourceType.SUPPLY;
            default -> ResourceType.SUPPLY;
        };
    }

    public static class RequestLineItemOption {
        private final Long requestId;
        private final String requestNumber;
        private final Long requestLineItemId;
        private final ResourceType resourceType;
        private final String resourceReference;
        private final Integer quantity;
        private final Integer fulfilledQuantity;

        public RequestLineItemOption(
                Long requestId,
                String requestNumber,
                Long requestLineItemId,
                ResourceType resourceType,
                String resourceReference,
                Integer quantity,
                Integer fulfilledQuantity) {
            this.requestId = requestId;
            this.requestNumber = requestNumber;
            this.requestLineItemId = requestLineItemId;
            this.resourceType = resourceType;
            this.resourceReference = resourceReference;
            this.quantity = quantity;
            this.fulfilledQuantity = fulfilledQuantity;
        }

        public Long getRequestId() {
            return requestId;
        }

        public String getRequestNumber() {
            return requestNumber;
        }

        public Long getRequestLineItemId() {
            return requestLineItemId;
        }

        public ResourceType getResourceType() {
            return resourceType;
        }

        public String getResourceReference() {
            return resourceReference;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public Integer getFulfilledQuantity() {
            return fulfilledQuantity;
        }

        public Integer getRemainingQuantity() {
            int fulfilled = fulfilledQuantity == null ? 0 : fulfilledQuantity;
            int requested = quantity == null ? 0 : quantity;
            return Math.max(0, requested - fulfilled);
        }

        public String getLabel() {
            String requestLabel = requestNumber != null && !requestNumber.isBlank() ? requestNumber : "REQ-" + requestId;
            String resourceLabel = resourceReference != null && !resourceReference.isBlank()
                    ? resourceReference
                    : resourceType == null ? "Resource" : resourceType.name();
            return requestLabel + " • " + resourceLabel + " • Item " + requestLineItemId + " • Qty " + getRemainingQuantity();
        }
    }
}
