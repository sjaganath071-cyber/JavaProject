package com.team11.defense_logistics.service;

import com.team11.defense_logistics.dto.AuditHistoryDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;

@Service
public class AuditHistoryService {

    private final String apiUrl;
    private final RestClient restClient;

    public AuditHistoryService(@Value("${defense-logistics.audit-history.api-url:}") String apiUrl) {
        this.apiUrl = apiUrl == null ? "" : apiUrl.trim();
        this.restClient = this.apiUrl.isBlank() ? null : RestClient.builder().baseUrl(this.apiUrl).build();
    }

    public boolean isApiConfigured() {
        return restClient != null;
    }

    public List<AuditHistoryDto> fetchAuditHistory() {
        if (!isApiConfigured()) {
            throw new IllegalStateException("Audit history API endpoint is not configured.");
        }

        try {
            return restClient.get()
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });
        } catch (Exception ex) {
            throw new IllegalStateException("The audit history service is temporarily unavailable.", ex);
        }
    }

    public List<AuditHistoryDto> emptyAuditHistory() {
        return Collections.emptyList();
    }
}
