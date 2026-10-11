package com.team11.defense_logistics.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.team11.defense_logistics.dto.LogisticsRequestDto;
import com.team11.defense_logistics.dto.VehicleDto;
import com.team11.defense_logistics.model.ResourceAllocation;
import com.team11.defense_logistics.service.LogisticsRequestService;
import com.team11.defense_logistics.service.ResourceAllocationService;
import com.team11.defense_logistics.service.VehicleService;

@Controller
@RequestMapping("/allocations")
public class ResourceAllocationController {

    private final ResourceAllocationService service;
    private final LogisticsRequestService requestService;
    private final VehicleService vehicleService;

    public ResourceAllocationController(
            ResourceAllocationService service,
            LogisticsRequestService requestService,
            VehicleService vehicleService) {
        this.service = service;
        this.requestService = requestService;
        this.vehicleService = vehicleService;
    }

    @GetMapping
    public String list(Model model) {
        addListData(model);
        model.addAttribute("allocation", new ResourceAllocation());
        return "allocations/list";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("allocation") ResourceAllocation allocation,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            addListData(model);
            return "allocations/list";
        }

        Long lineItemRequestId = requestService.getRequestIdForLineItem(allocation.getRequestLineItemId());
        if (lineItemRequestId != null && !lineItemRequestId.equals(allocation.getRequestId())) {
            bindingResult.reject("allocation.invalid", "The selected request line item does not belong to the selected request.");
            addListData(model);
            return "allocations/list";
        }

        try {
            service.create(allocation);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Resource allocation created successfully.");
            return "redirect:/allocations";
        } catch (RuntimeException exception) {
            bindingResult.reject("allocation.invalid", exception.getMessage());
            addListData(model);
            return "allocations/list";
        }
    }

    @PostMapping("/{id}/release")
    public String release(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        try {
            service.release(id);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Resource allocation released.");
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage());
        }
        return "redirect:/allocations";
    }

    @PostMapping("/{id}/cancel")
    public String cancel(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        try {
            service.cancel(id);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Resource allocation cancelled.");
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage());
        }
        return "redirect:/allocations";
    }

    private void addListData(Model model) {
        try {
            List<LogisticsRequestDto> approvedRequests = requestService.getAllRequests().stream()
                    .filter(request -> "APPROVED".equalsIgnoreCase(request.getStatus()))
                    .toList();
            List<VehicleDto> availableVehicles = vehicleService.getAllVehicles().stream()
                    .filter(vehicle -> "AVAILABLE".equalsIgnoreCase(vehicle.getStatus()))
                    .toList();
            model.addAttribute("allocations", service.findAll());
            model.addAttribute("approvedRequests", approvedRequests);
            model.addAttribute("requestLineItems", requestService.getApprovedRequestLineItems());
            model.addAttribute("availableVehicles", availableVehicles);
        } catch (IllegalStateException exception) {
            model.addAttribute("allocations", List.of());
            model.addAttribute("approvedRequests", List.of());
            model.addAttribute("requestLineItems", List.of());
            model.addAttribute("availableVehicles", List.of());
            model.addAttribute("errorMessage", exception.getMessage());
        }
    }
}
