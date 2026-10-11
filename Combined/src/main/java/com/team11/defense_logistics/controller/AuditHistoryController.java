package com.team11.defense_logistics.controller;

import com.team11.defense_logistics.dto.AuditHistoryDto;
import com.team11.defense_logistics.service.AuditHistoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Controller
public class AuditHistoryController {

    private final AuditHistoryService auditHistoryService;

    public AuditHistoryController(AuditHistoryService auditHistoryService) {
        this.auditHistoryService = auditHistoryService;
    }

    @GetMapping("/audit-history")
    public String auditHistory(Model model) {
        List<AuditHistoryDto> records = Collections.emptyList();
        String auditError = null;

        try {
            if (!auditHistoryService.isApiConfigured()) {
                auditError = "The audit history backend endpoint is not configured yet. Please configure defense-logistics.audit-history.api-url.";
            } else {
                records = auditHistoryService.fetchAuditHistory();
            }
        } catch (Exception ex) {
            auditError = "The audit history service is currently unavailable. Please try again later.";
        }

        String latestTimestamp = "N/A";
        long categoryCount = 0L;

        if (!records.isEmpty()) {
            OffsetDateTime latest = records.stream()
                    .map(AuditHistoryDto::getTimestamp)
                    .filter(timestamp -> timestamp != null)
                    .max(OffsetDateTime::compareTo)
                    .orElse(null);

            if (latest != null) {
                latestTimestamp = latest.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
            }

            categoryCount = records.stream()
                    .map(AuditHistoryDto::getModule)
                    .filter(module -> module != null && !module.isBlank())
                    .distinct()
                    .count();
        }

        model.addAttribute("auditRecords", records);
        model.addAttribute("auditError", auditError);
        model.addAttribute("totalAuditRecords", records.size());
        model.addAttribute("latestAuditTimestamp", latestTimestamp);
        model.addAttribute("distinctAuditCategories", categoryCount);

        return "audit-history";
    }
}
