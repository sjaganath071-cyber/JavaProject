package com.team11.defense_logistics.controller;

import com.team11.defense_logistics.dto.MaintenanceDto;
import com.team11.defense_logistics.service.MaintenanceService;
import com.team11.defense_logistics.service.MaintenanceService.MaintenanceServiceException;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/maintenance")
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    public MaintenanceController(MaintenanceService maintenanceService) {
        this.maintenanceService = maintenanceService;
    }

    @GetMapping
    public String listRecords(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String maintenanceType,
            Model model) {

        try {
            List<MaintenanceDto> allRecords = maintenanceService.getAllRecords();

            List<MaintenanceDto> filteredRecords = allRecords.stream()
                    .filter(record -> matchesKeyword(record, keyword))
                    .filter(record -> matchesStatus(record, status))
                    .filter(record -> matchesType(record, maintenanceType))
                    .collect(Collectors.toList());

            long scheduledCount = allRecords.stream()
                    .filter(record -> "SCHEDULED".equalsIgnoreCase(record.getStatus()))
                    .count();

            long inProgressCount = allRecords.stream()
                    .filter(record -> "IN_PROGRESS".equalsIgnoreCase(record.getStatus()))
                    .count();

            long completedCount = allRecords.stream()
                    .filter(record -> "COMPLETED".equalsIgnoreCase(record.getStatus()))
                    .count();

            model.addAttribute("records", filteredRecords);
            model.addAttribute("keyword", keyword == null ? "" : keyword);
            model.addAttribute("selectedStatus", status == null ? "" : status);
            model.addAttribute(
                    "selectedMaintenanceType",
                    maintenanceType == null ? "" : maintenanceType
            );
            model.addAttribute("totalRecords", allRecords.size());
            model.addAttribute("scheduledCount", scheduledCount);
            model.addAttribute("inProgressCount", inProgressCount);
            model.addAttribute("completedCount", completedCount);

        } catch (MaintenanceServiceException exception) {
            model.addAttribute("records", List.of());
            model.addAttribute("keyword", keyword == null ? "" : keyword);
            model.addAttribute("selectedStatus", status == null ? "" : status);
            model.addAttribute(
                    "selectedMaintenanceType",
                    maintenanceType == null ? "" : maintenanceType
            );
            model.addAttribute("totalRecords", 0);
            model.addAttribute("scheduledCount", 0);
            model.addAttribute("inProgressCount", 0);
            model.addAttribute("completedCount", 0);
            model.addAttribute("errorMessage", exception.getMessage());
        }

        return "maintenance/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        MaintenanceDto record = new MaintenanceDto();
        record.setStatus("SCHEDULED");
        record.setResourceType("EQUIPMENT");
        record.setMaintenanceType("PREVENTIVE");

        model.addAttribute("maintenance", record);
        model.addAttribute("formTitle", "Schedule Maintenance");
        model.addAttribute("formAction", "/maintenance");

        return "maintenance/form";
    }

    @PostMapping
    public String createRecord(
            @Valid @ModelAttribute("maintenance") MaintenanceDto record,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("formTitle", "Schedule Maintenance");
            model.addAttribute("formAction", "/maintenance");
            return "maintenance/form";
        }

        try {
            maintenanceService.createRecord(record);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Maintenance record created successfully."
            );

            return "redirect:/maintenance";

        } catch (MaintenanceServiceException exception) {
            model.addAttribute("formTitle", "Schedule Maintenance");
            model.addAttribute("formAction", "/maintenance");
            model.addAttribute("errorMessage", exception.getMessage());

            return "maintenance/form";
        }
    }

    @GetMapping("/{id}")
    public String viewRecord(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            model.addAttribute(
                    "maintenance",
                    maintenanceService.getRecordById(id)
            );

            return "maintenance/details";

        } catch (MaintenanceServiceException exception) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );

            return "redirect:/maintenance";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            model.addAttribute(
                    "maintenance",
                    maintenanceService.getRecordById(id)
            );

            model.addAttribute("formTitle", "Edit Maintenance Record");
            model.addAttribute("formAction", "/maintenance/" + id);

            return "maintenance/form";

        } catch (MaintenanceServiceException exception) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );

            return "redirect:/maintenance";
        }
    }

    @PostMapping("/{id}")
    public String updateRecord(
            @PathVariable Long id,
            @Valid @ModelAttribute("maintenance") MaintenanceDto record,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("formTitle", "Edit Maintenance Record");
            model.addAttribute("formAction", "/maintenance/" + id);
            return "maintenance/form";
        }

        try {
            maintenanceService.updateRecord(id, record);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Maintenance record updated successfully."
            );

            return "redirect:/maintenance/" + id;

        } catch (MaintenanceServiceException exception) {
            model.addAttribute("formTitle", "Edit Maintenance Record");
            model.addAttribute("formAction", "/maintenance/" + id);
            model.addAttribute("errorMessage", exception.getMessage());

            return "maintenance/form";
        }
    }

    private boolean matchesKeyword(MaintenanceDto record, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }

        String search = keyword.trim().toLowerCase(Locale.ROOT);

        return contains(record.getMaintenanceCode(), search)
                || contains(record.getResourceCode(), search)
                || contains(record.getDescription(), search)
                || contains(record.getAssignedTechnician(), search);
    }

    private boolean matchesStatus(MaintenanceDto record, String status) {
        return status == null
                || status.isBlank()
                || status.equalsIgnoreCase(record.getStatus());
    }

    private boolean matchesType(
            MaintenanceDto record,
            String maintenanceType) {

        return maintenanceType == null
                || maintenanceType.isBlank()
                || maintenanceType.equalsIgnoreCase(record.getMaintenanceType());
    }

    private boolean contains(String value, String search) {
        return value != null
                && value.toLowerCase(Locale.ROOT).contains(search);
    }
}
