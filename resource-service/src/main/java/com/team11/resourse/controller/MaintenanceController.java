package com.team11.resourse.controller;

import com.team11.resourse.dto.MaintenanceCreateRequest;
import com.team11.resourse.dto.MaintenanceNotesRequest;
import com.team11.resourse.dto.MaintenanceResponse;
import com.team11.resourse.dto.MaintenanceStatusRequest;
import com.team11.resourse.dto.MaintenanceUpdateRequest;
import com.team11.resourse.service.MaintenanceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/maintenance")
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    public MaintenanceController(MaintenanceService maintenanceService) {
        this.maintenanceService = maintenanceService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MaintenanceResponse create(@Valid @RequestBody MaintenanceCreateRequest request) {
        return maintenanceService.create(request);
    }

    @GetMapping
    public List<MaintenanceResponse> getAll(
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) Long resourceId,
            @RequestParam(required = false) String status) {
        return maintenanceService.getAll(resourceType, resourceId, status);
    }

    @GetMapping("/upcoming")
    public List<MaintenanceResponse> getUpcoming() {
        return maintenanceService.getUpcoming();
    }

    @GetMapping("/overdue")
    public List<MaintenanceResponse> getOverdue() {
        return maintenanceService.getOverdue();
    }

    @GetMapping("/{id}")
    public MaintenanceResponse getById(@PathVariable Long id) {
        return maintenanceService.getById(id);
    }

    @PutMapping("/{id}")
    public MaintenanceResponse update(
            @PathVariable Long id,
            @Valid @RequestBody MaintenanceUpdateRequest request) {
        return maintenanceService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    public MaintenanceResponse updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody MaintenanceStatusRequest request) {
        return maintenanceService.updateStatus(id, request);
    }

    @PatchMapping("/{id}/notes")
    public MaintenanceResponse updateNotes(
            @PathVariable Long id,
            @Valid @RequestBody MaintenanceNotesRequest request) {
        return maintenanceService.updateNotes(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        maintenanceService.delete(id);
    }
}