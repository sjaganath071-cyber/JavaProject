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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.team11.defense_logistics.dto.VehicleDto;
import com.team11.defense_logistics.model.Movement;
import com.team11.defense_logistics.model.ResourceAllocation;
import com.team11.defense_logistics.service.MovementService;
import com.team11.defense_logistics.service.ResourceAllocationService;
import com.team11.defense_logistics.service.VehicleService;

@Controller
@RequestMapping("/movements")
public class MovementController {

    private final MovementService movementService;
    private final VehicleService vehicleService;
    private final ResourceAllocationService allocationService;

    public MovementController(
            MovementService movementService,
            VehicleService vehicleService,
            ResourceAllocationService allocationService) {
        this.movementService = movementService;
        this.vehicleService = vehicleService;
        this.allocationService = allocationService;
    }

    @GetMapping
    public String list(Model model) {
        addListData(model);
        model.addAttribute("movement", new Movement());
        return "movements/list";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("movement") Movement movement,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            addListData(model);
            return "movements/list";
        }

        try {
            movementService.create(movement);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Movement created successfully.");
            return "redirect:/movements";
        } catch (RuntimeException exception) {
            bindingResult.reject("movement.invalid", exception.getMessage());
            addListData(model);
            return "movements/list";
        }
    }

    @PostMapping("/{id}/status")
    public String updateStatus(
            @PathVariable Long id,
            @RequestParam String status,
            RedirectAttributes redirectAttributes) {
        try {
            movementService.updateStatus(id, status);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Movement status updated.");
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage());
        }
        return "redirect:/movements";
    }

    private void addListData(Model model) {
        try {
            List<VehicleDto> vehicles = vehicleService.getAllVehicles();
            List<ResourceAllocation> activeAllocations = allocationService.byStatus("Allocated");
            model.addAttribute("movements", movementService.findAll());
            model.addAttribute("vehicles", vehicles);
            model.addAttribute("activeAllocations", activeAllocations);
        } catch (IllegalStateException exception) {
            model.addAttribute("movements", List.of());
            model.addAttribute("vehicles", List.of());
            model.addAttribute("activeAllocations", List.of());
            model.addAttribute("errorMessage", exception.getMessage());
        }
    }
}
