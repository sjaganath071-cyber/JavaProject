package com.team11.defense_logistics.controller;

import com.team11.defense_logistics.dto.DashboardSummaryDto;
import com.team11.defense_logistics.service.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Controller
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        DashboardSummaryDto summary = new DashboardSummaryDto();

        try {
            summary = dashboardService.getDashboardSummary();
        } catch (Exception exception) {
            summary.setLastUpdated(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            summary.setStatusMessage("Dashboard data is currently unavailable because the connected backend services are not responding.");
            summary.setPersonnelDataAvailable(false);
            summary.setEquipmentDataAvailable(false);
            summary.setVehicleDataAvailable(false);
            summary.setMaintenanceDataAvailable(false);
            summary.setLogisticsRequestDataAvailable(false);
        }

        List<Map<String, String>> recentRequests = dashboardService.getRecentRequests();

        model.addAttribute("summary", summary);
        model.addAttribute("recentRequests", recentRequests);
        model.addAttribute("developmentMode", false);
        model.addAttribute("errorMessage", summary.getStatusMessage());

        return "dashboard";
    }
}