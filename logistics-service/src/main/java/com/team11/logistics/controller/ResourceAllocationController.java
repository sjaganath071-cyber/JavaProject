package com.team11.logistics.controller;

import com.team11.logistics.dto.AllocationResponse;
import com.team11.logistics.dto.CreateAllocationDto;
import com.team11.logistics.model.AllocationStatus;
import com.team11.logistics.service.ResourceAllocationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/allocations")
@Validated
public class ResourceAllocationController {

    private final ResourceAllocationService resourceAllocationService;

    public ResourceAllocationController(ResourceAllocationService resourceAllocationService) {
        this.resourceAllocationService = resourceAllocationService;
    }

    @PostMapping("/requests/{requestId}")
    public ResponseEntity<AllocationResponse> createAllocation(
            @PathVariable @Positive Long requestId,
            @Valid @RequestBody CreateAllocationDto dto) {

        AllocationResponse response = resourceAllocationService.createAllocation(requestId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<AllocationResponse>> listAllocations(
            @RequestParam(required = false) Long requestId,
            @RequestParam(required = false) AllocationStatus status) {

        return ResponseEntity.ok(resourceAllocationService.listAllocations(requestId, status));
    }

    @GetMapping("/{allocationId}")
    public ResponseEntity<AllocationResponse> getAllocation(
            @PathVariable @Positive Long allocationId) {

        return ResponseEntity.ok(resourceAllocationService.getAllocation(allocationId));
    }

    @PostMapping("/{allocationId}/release")
    public ResponseEntity<AllocationResponse> releaseAllocation(
            @PathVariable @Positive Long allocationId) {

        return ResponseEntity.ok(resourceAllocationService.releaseAllocation(allocationId));
    }

    @PostMapping("/{allocationId}/complete")
    public ResponseEntity<AllocationResponse> completeAllocation(
            @PathVariable @Positive Long allocationId) {

        return ResponseEntity.ok(resourceAllocationService.completeAllocation(allocationId));
    }
}
