package com.team11.defense_logistics.service;

import com.team11.defense_logistics.dto.NotificationDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;

@Service
public class NotificationService {

    private final String apiUrl;
    private final RestClient restClient;

    public NotificationService(@Value("${defense-logistics.notifications.api-url:}") String apiUrl) {
        this.apiUrl = apiUrl == null ? "" : apiUrl.trim();
        this.restClient = this.apiUrl.isBlank() ? null : RestClient.builder().baseUrl(this.apiUrl).build();
    }

    public boolean isApiConfigured() {
        return restClient != null;
    }

    public List<NotificationDto> fetchNotifications() {
        if (!isApiConfigured()) {
            throw new IllegalStateException("Notification API endpoint is not configured.");
        }

        try {
            return restClient.get()
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });
        } catch (Exception ex) {
            throw new IllegalStateException("The notifications service is temporarily unavailable.", ex);
        }
    }

    public List<NotificationDto> emptyNotifications() {
        return Collections.emptyList();
    }
}
