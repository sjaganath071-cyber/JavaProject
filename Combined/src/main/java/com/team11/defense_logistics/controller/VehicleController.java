package com.team11.defense_logistics.controller;

import com.team11.defense_logistics.dto.VehicleDto;
import com.team11.defense_logistics.service.VehicleService;
import com.team11.defense_logistics.service.VehicleService.VehicleServiceException;

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

@Controller
@RequestMapping("/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @GetMapping
    public String listVehicles(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            Model model) {

        try {
            List<VehicleDto> allVehicles = vehicleService.getAllVehicles();

            List<VehicleDto> filteredVehicles = allVehicles.stream()
                    .filter(vehicle -> matchesKeyword(vehicle, keyword))
                    .filter(vehicle -> matchesType(vehicle, type))
                    .filter(vehicle -> matchesStatus(vehicle, status))
                    .toList();

            model.addAttribute("vehicleList", filteredVehicles);
            model.addAttribute("keyword", keyword == null ? "" : keyword);
            model.addAttribute("type", type == null ? "" : type);
            model.addAttribute("status", status == null ? "" : status);

            model.addAttribute(
                    "vehicleTypes",
                    allVehicles.stream()
                            .map(VehicleDto::getType)
                            .filter(value -> value != null && !value.isBlank())
                            .distinct()
                            .sorted()
                            .toList()
            );

        } catch (VehicleServiceException exception) {
            model.addAttribute("vehicleList", List.of());
            model.addAttribute("vehicleTypes", List.of());
            model.addAttribute("keyword", keyword == null ? "" : keyword);
            model.addAttribute("type", type == null ? "" : type);
            model.addAttribute("status", status == null ? "" : status);
            model.addAttribute("errorMessage", exception.getMessage());
        }

        return "vehicles/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("vehicle", new VehicleDto());
        model.addAttribute("pageTitle", "Register Vehicle");
        model.addAttribute("formAction", "/vehicles/new");
        model.addAttribute("formMode", "create");

        return "vehicles/form";
    }

    @PostMapping("/new")
    public String createVehicle(
            @Valid @ModelAttribute("vehicle") VehicleDto vehicle,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Register Vehicle");
            model.addAttribute("formAction", "/vehicles/new");
            model.addAttribute("formMode", "create");

            return "vehicles/form";
        }

        try {
            vehicleService.createVehicle(vehicle);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Vehicle registered successfully."
            );

            return "redirect:/vehicles";

        } catch (VehicleServiceException exception) {
            model.addAttribute("pageTitle", "Register Vehicle");
            model.addAttribute("formAction", "/vehicles/new");
            model.addAttribute("formMode", "create");
            model.addAttribute("errorMessage", exception.getMessage());

            return "vehicles/form";
        }
    }

    @GetMapping("/{id}")
    public String viewVehicle(
            @PathVariable Long id,
            Model model) {

        try {
            model.addAttribute(
                    "vehicle",
                    vehicleService.getVehicleById(id)
            );

            return "vehicles/details";

        } catch (VehicleServiceException exception) {
            model.addAttribute("errorMessage", exception.getMessage());

            return "vehicles/details";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(
            @PathVariable Long id,
            Model model) {

        try {
            model.addAttribute(
                    "vehicle",
                    vehicleService.getVehicleById(id)
            );

            model.addAttribute("pageTitle", "Edit Vehicle");
            model.addAttribute("formAction", "/vehicles/" + id + "/edit");
            model.addAttribute("formMode", "edit");

            return "vehicles/form";

        } catch (VehicleServiceException exception) {
            model.addAttribute("errorMessage", exception.getMessage());

            return "vehicles/details";
        }
    }

    @PostMapping("/{id}/edit")
    public String updateVehicle(
            @PathVariable Long id,
            @Valid @ModelAttribute("vehicle") VehicleDto vehicle,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Edit Vehicle");
            model.addAttribute("formAction", "/vehicles/" + id + "/edit");
            model.addAttribute("formMode", "edit");

            return "vehicles/form";
        }

        try {
            vehicleService.updateVehicle(id, vehicle);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Vehicle updated successfully."
            );

            return "redirect:/vehicles/" + id;

        } catch (VehicleServiceException exception) {
            model.addAttribute("pageTitle", "Edit Vehicle");
            model.addAttribute("formAction", "/vehicles/" + id + "/edit");
            model.addAttribute("formMode", "edit");
            model.addAttribute("errorMessage", exception.getMessage());

            return "vehicles/form";
        }
    }

    private boolean matchesKeyword(VehicleDto vehicle, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }

        String search = keyword.toLowerCase(Locale.ROOT);

        return contains(vehicle.getVehicleCode(), search)
                || contains(vehicle.getName(), search)
                || contains(vehicle.getType(), search)
                || contains(vehicle.getUnit(), search)
                || contains(vehicle.getLocation(), search);
    }

    private boolean matchesType(VehicleDto vehicle, String type) {
        return type == null
                || type.isBlank()
                || (vehicle.getType() != null
                && vehicle.getType().equalsIgnoreCase(type));
    }

    private boolean matchesStatus(VehicleDto vehicle, String status) {
        return status == null
                || status.isBlank()
                || (vehicle.getStatus() != null
                && vehicle.getStatus().equalsIgnoreCase(status));
    }

    private boolean contains(String value, String search) {
        return value != null
                && value.toLowerCase(Locale.ROOT).contains(search);
    }
}
