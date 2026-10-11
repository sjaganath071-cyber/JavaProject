package com.team11.defense_logistics.controller;

import com.team11.defense_logistics.dto.ReadinessReportDto;
import com.team11.defense_logistics.service.ReadinessReportService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
public class ReadinessReportController {

    private final ReadinessReportService readinessReportService;

    public ReadinessReportController(ReadinessReportService readinessReportService) {
        this.readinessReportService = readinessReportService;
    }

    @GetMapping("/reports/readiness")
    public String readiness(Model model) {
        try {
            ReadinessReportDto report = readinessReportService.getReadinessReport();
            if (report.getGeneratedAt() == null || report.getGeneratedAt().isBlank()) {
                report.setGeneratedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")));
            }
            model.addAttribute("report", report);
            return "reports/readiness";
        } catch (Exception exception) {
            ReadinessReportDto fallbackReport = new ReadinessReportDto();
            fallbackReport.setGeneratedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")));
            fallbackReport.setMessage("Readiness data is currently unavailable. The connected backend services are not responding or their contracts are not yet confirmed.");
            fallbackReport.setPersonnelDataAvailable(false);
            fallbackReport.setEquipmentDataAvailable(false);
            fallbackReport.setVehicleDataAvailable(false);
            model.addAttribute("report", fallbackReport);
            return "reports/readiness";
        }
    }
}
