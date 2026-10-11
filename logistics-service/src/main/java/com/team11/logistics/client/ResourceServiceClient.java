package com.team11.logistics.client;

import com.team11.logistics.dto.ResourceAvailabilityResponse;
import com.team11.logistics.dto.ResourceReservationRequest;
import com.team11.logistics.dto.ResourceReservationResponse;
import com.team11.logistics.model.ResourceType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class ResourceServiceClient {

    private final RestClient restClient = RestClient.create();
    private final ResourceServiceProperties properties;

    public ResourceServiceClient(ResourceServiceProperties properties) {
        this.properties = properties;
    }

    public boolean isConfigured() {
        return StringUtils.hasText(properties.getBaseUrl());
    }

    public String getBaseUrl() {
        return properties.getBaseUrl();
    }

    public ResourceAvailabilityResponse checkAvailability(ResourceType resourceType, String resourceReference, Integer quantity) {
        if (!isConfigured()) {
            return new ResourceAvailabilityResponse(false,
                    "Resource Service base URL is not configured. Kaviya must confirm the availability contract.");
        }

        String formattedUrl = properties.getBaseUrl().replaceAll("/$", "")
                + "/api/resources/availability?resourceType=" + resourceType
                + "&resourceReference=" + URLEncoder.encode(resourceReference, StandardCharsets.UTF_8)
                + "&quantity=" + quantity;

        try {
            return restClient.get()
                    .uri(formattedUrl)
                    .retrieve()
                    .body(ResourceAvailabilityResponse.class);
        } catch (RestClientException ex) {
            return new ResourceAvailabilityResponse(false,
                    "Resource Service availability check failed: " + ex.getMessage());
        }
    }

    public ResourceReservationResponse reserveResource(ResourceReservationRequest request) {
        if (!isConfigured()) {
            return new ResourceReservationResponse(false, null,
                    "Resource Service base URL is not configured. Kaviya must confirm the reservation contract.");
        }

        String formattedUrl = properties.getBaseUrl().replaceAll("/$", "") + "/api/resources/reservations";

        try {
            ResourceReservationResponse response = restClient.post()
                    .uri(formattedUrl)
                    .body(request)
                    .retrieve()
                    .body(ResourceReservationResponse.class);

            if (response == null) {
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                        "Resource Service reservation returned an empty response.");
            }

            return response;
        } catch (RestClientException ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Resource Service reservation failed: " + ex.getMessage(), ex);
        }
    }
}
