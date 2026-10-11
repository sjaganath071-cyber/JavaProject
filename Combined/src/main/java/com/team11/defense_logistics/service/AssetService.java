package com.team11.defense_logistics.service;

import com.team11.defense_logistics.dto.AssetDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AssetService {

    private final RestClient restClient;
    private final String assetApiUrl;

    public AssetService(
            RestClient.Builder restClientBuilder,
            @Value("${services.asset.base-url:http://localhost:8081/api/v1/assets}")
            String assetApiUrl) {

        this.restClient = restClientBuilder.build();
        this.assetApiUrl = assetApiUrl;
    }

    public List<AssetDto> getAllAssets() {
        try {
            List<Map<String, Object>> assets = restClient.get()
                    .uri(assetApiUrl)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});

            if (assets == null || assets.isEmpty()) {
                return new ArrayList<>();
            }

            List<AssetDto> mapped = new ArrayList<>();
            for (Map<String, Object> asset : assets) {
                mapped.add(mapAsset(asset));
            }
            return mapped;

        } catch (RestClientException exception) {
            throw new AssetServiceException(
                    "Unable to retrieve assets from the backend service.",
                    exception
            );
        }
    }

    public AssetDto getAssetById(Long id) {
        try {
            Map<String, Object> asset = restClient.get()
                    .uri(assetApiUrl + "/{id}", id)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});

            if (asset == null) {
                throw new AssetServiceException(
                        "The backend returned no asset record."
                );
            }

            return mapAsset(asset);

        } catch (RestClientException exception) {
            throw new AssetServiceException(
                    "Unable to retrieve asset record " + id + ".",
                    exception
            );
        }
    }

    public AssetDto createAsset(AssetDto asset) {
        try {
            Map<String, Object> request = toAssetRequest(asset);

            Map<String, Object> created = restClient.post()
                    .uri(assetApiUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});

            if (created == null) {
                throw new AssetServiceException(
                        "The backend returned no created asset record."
                );
            }

            return mapAsset(created);

        } catch (RestClientException exception) {
            throw new AssetServiceException(
                    "Unable to create the asset. Check the submitted information.",
                    exception
            );
        }
    }

    public AssetDto updateAsset(Long id, AssetDto asset) {
        try {
            Map<String, Object> request = toAssetRequest(asset);

            Map<String, Object> updated = restClient.put()
                    .uri(assetApiUrl + "/{id}", id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});

            if (updated == null) {
                throw new AssetServiceException(
                        "The backend returned no updated asset record."
                );
            }

            return mapAsset(updated);

        } catch (RestClientException exception) {
            throw new AssetServiceException(
                    "Unable to update asset record " + id + ".",
                    exception
            );
        }
    }

    private AssetDto mapAsset(Map<String, Object> payload) {
        AssetDto dto = new AssetDto();
        dto.setId(asLong(payload.get("assetId")));
        dto.setAssetTag(safeString(payload.get("assetTag")));
        dto.setName(safeString(payload.get("assetName")));
        dto.setCategory(safeString(payload.get("category")));
        dto.setUnit(safeString(payload.get("unitId")));
        dto.setLocation(safeString(payload.get("location")));
        dto.setCondition(safeString(payload.get("condition")));
        dto.setStatus(safeString(payload.get("status")));
        return dto;
    }

    private Map<String, Object> toAssetRequest(AssetDto asset) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("assetTag", asset.getAssetTag());
        request.put("assetName", asset.getName());
        request.put("category", asset.getCategory());
        request.put("unitId", parseLong(asset.getUnit()));
        request.put("location", asset.getLocation());
        request.put("condition", asset.getCondition());
        request.put("status", asset.getStatus());
        return request;
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private Long asLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            try {
                return Long.parseLong(text.trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private String safeString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    public static class AssetServiceException extends RuntimeException {

        public AssetServiceException(String message) {
            super(message);
        }

        public AssetServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
