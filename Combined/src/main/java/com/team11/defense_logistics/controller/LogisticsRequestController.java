package com.team11.defense_logistics.controller;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import com.team11.defense_logistics.dto.LogisticsRequestDto;
import com.team11.defense_logistics.service.LogisticsRequestService;

@Controller
@RequestMapping("/logistics-requests")
public class LogisticsRequestController {

    private static final Logger logger = LoggerFactory.getLogger(LogisticsRequestController.class);

    private final LogisticsRequestService logisticsRequestService;

    public LogisticsRequestController(LogisticsRequestService logisticsRequestService) {
        this.logisticsRequestService = logisticsRequestService;
    }

    @GetMapping
    public String listRequests(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String status,
            Model model) {

        List<LogisticsRequestDto> allRequests;
        boolean dataAvailable = true;
        try {
            allRequests = logisticsRequestService.getAllRequests();
        } catch (IllegalStateException ex) {
            allRequests = List.of();
            dataAvailable = false;
            logger.warn("Unable to retrieve logistics requests.", ex);
        }

        List<LogisticsRequestDto> filteredRequests = allRequests.stream()
                .filter(request -> matchesKeyword(request, keyword))
                .filter(request -> matchesValue(request.getPriority(), priority))
                .filter(request -> matchesValue(request.getStatus(), status))
                .collect(Collectors.toList());

        long pendingCount = allRequests.stream()
                .filter(request -> equalsIgnoreCase(request.getStatus(), "PENDING"))
                .count();

        long approvedCount = allRequests.stream()
                .filter(request -> equalsIgnoreCase(request.getStatus(), "APPROVED"))
                .count();

        long allocatedCount = allRequests.stream()
                .filter(request -> equalsIgnoreCase(request.getStatus(), "ALLOCATED"))
                .count();

        long criticalCount = allRequests.stream()
                .filter(request -> equalsIgnoreCase(request.getPriority(), "CRITICAL"))
                .filter(request -> !equalsIgnoreCase(request.getStatus(), "COMPLETED"))
                .filter(request -> !equalsIgnoreCase(request.getStatus(), "REJECTED"))
                .count();

        model.addAttribute("requests", filteredRequests);
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("selectedPriority", priority == null ? "" : priority);
        model.addAttribute("selectedStatus", status == null ? "" : status);
        model.addAttribute("dataAvailable", dataAvailable);

        model.addAttribute("totalCount", allRequests.size());
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("approvedCount", approvedCount);
        model.addAttribute("allocatedCount", allocatedCount);
        model.addAttribute("criticalCount", criticalCount);

        return "logistics-requests/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        LogisticsRequestDto request = new LogisticsRequestDto();
        request.setRequestedDate(java.time.LocalDate.now());

        model.addAttribute("request", request);
        model.addAttribute("pageTitle", "Create Logistics Request");
        model.addAttribute("formAction", "/logistics-requests/new");

        return "logistics-requests/form";
    }

    @PostMapping("/new")
    public String createRequest(
            @Valid @ModelAttribute("request") LogisticsRequestDto request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        validateDates(request, bindingResult);

        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Create Logistics Request");
            model.addAttribute("formAction", "/logistics-requests/new");
            return "logistics-requests/form";
        }

        try {
            logisticsRequestService.createRequest(request);
            redirectAttributes.addFlashAttribute(
                    "successMessage", "Logistics request created successfully.");

            return "redirect:/logistics-requests";

        } catch (IllegalStateException ex) {
            model.addAttribute("pageTitle", "Create Logistics Request");
            model.addAttribute("formAction", "/logistics-requests/new");
            model.addAttribute("errorMessage",
                    "Unable to save the request. Please check backend connectivity.");
            return "logistics-requests/form";
        }
    }

    @GetMapping("/{id}")
    public String viewRequest(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            LogisticsRequestDto request =
                    logisticsRequestService.getRequestById(id);

            if (request == null) {
                redirectAttributes.addFlashAttribute(
                        "errorMessage", "Logistics request was not found.");
                return "redirect:/logistics-requests";
            }

            model.addAttribute("request", request);
            return "logistics-requests/details";

        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "Unable to retrieve the request from the backend.");
            return "redirect:/logistics-requests";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            LogisticsRequestDto request =
                    logisticsRequestService.getRequestById(id);

            if (request == null) {
                redirectAttributes.addFlashAttribute(
                        "errorMessage", "Logistics request was not found.");
                return "redirect:/logistics-requests";
            }

            model.addAttribute("request", request);
            model.addAttribute("pageTitle", "Edit Logistics Request");
            model.addAttribute("formAction",
                    "/logistics-requests/" + id + "/edit");

            return "logistics-requests/form";

        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "Unable to retrieve the request from the backend.");
            return "redirect:/logistics-requests";
        }
    }

    @PostMapping("/{id}/edit")
    public String updateRequest(
            @PathVariable Long id,
            @Valid @ModelAttribute("request") LogisticsRequestDto request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        validateDates(request, bindingResult);

        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Edit Logistics Request");
            model.addAttribute("formAction",
                    "/logistics-requests/" + id + "/edit");
            return "logistics-requests/form";
        }

        request.setId(id);

        try {
            logisticsRequestService.updateRequest(id, request);

            redirectAttributes.addFlashAttribute(
                    "successMessage", "Logistics request updated successfully.");

            return "redirect:/logistics-requests/" + id;

        } catch (IllegalStateException ex) {
            model.addAttribute("pageTitle", "Edit Logistics Request");
            model.addAttribute("formAction",
                    "/logistics-requests/" + id + "/edit");
            model.addAttribute("errorMessage",
                    "Unable to update the request. Please check backend connectivity.");
            return "logistics-requests/form";
        }
    }

    private boolean matchesKeyword(
            LogisticsRequestDto request,
            String keyword) {

        if (keyword == null || keyword.isBlank()) {
            return true;
        }

        String search = keyword.toLowerCase(Locale.ROOT);

        return contains(request.getRequestCode(), search)
                || contains(request.getRequestingUnit(), search)
                || contains(request.getDescription(), search)
                || contains(request.getAssignedResource(), search);
    }

    private boolean matchesValue(String actual, String expected) {
        return expected == null
                || expected.isBlank()
                || equalsIgnoreCase(actual, expected);
    }

    private boolean contains(String actual, String search) {
        return actual != null
                && actual.toLowerCase(Locale.ROOT).contains(search);
    }

    private boolean equalsIgnoreCase(String first, String second) {
        return first != null
                && second != null
                && first.equalsIgnoreCase(second);
    }

    private void validateDates(
            LogisticsRequestDto request,
            BindingResult bindingResult) {

        if (request.getRequestedDate() != null
                && request.getRequiredByDate() != null
                && request.getRequiredByDate()
                        .isBefore(request.getRequestedDate())) {

            bindingResult.rejectValue(
                    "requiredByDate",
                    "date.invalid",
                    "Required-by date cannot be before the requested date.");
        }
    }
}
