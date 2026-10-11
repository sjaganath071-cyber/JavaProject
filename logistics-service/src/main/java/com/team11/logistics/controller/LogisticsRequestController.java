package com.team11.logistics.controller;

import com.team11.logistics.dto.CreateLogisticsRequestDto;
import com.team11.logistics.dto.LogisticsRequestResponse;
import com.team11.logistics.dto.UpdateLogisticsRequestDto;
import com.team11.logistics.model.RequestPriority;
import com.team11.logistics.model.RequestStatus;
import com.team11.logistics.service.LogisticsRequestService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Validated
public class LogisticsRequestController {

    private final LogisticsRequestService logisticsRequestService;

    public LogisticsRequestController(LogisticsRequestService logisticsRequestService) {
        this.logisticsRequestService = logisticsRequestService;
    }

    @PostMapping("/requests")
    public ResponseEntity<LogisticsRequestResponse> createRequest(
            @Valid @RequestBody CreateLogisticsRequestDto requestDto) {

        LogisticsRequestResponse created = logisticsRequestService.createDraft(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/requests")
    public ResponseEntity<List<LogisticsRequestResponse>> listRequests(
            @RequestParam(required = false) RequestStatus status,
            @RequestParam(required = false) String requestingUnit,
            @RequestParam(required = false) RequestPriority priority,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdTo) {

        return ResponseEntity.ok(logisticsRequestService.listRequests(status, requestingUnit, priority, createdFrom, createdTo));
    }

    @GetMapping("/requests/{id}")
    public ResponseEntity<LogisticsRequestResponse> getRequest(
            @PathVariable @Positive Long id) {

        return ResponseEntity.ok(logisticsRequestService.getRequest(id));
    }

    @PutMapping("/requests/{id}")
    public ResponseEntity<LogisticsRequestResponse> updateRequest(
            @PathVariable @Positive Long id,
            @Valid @RequestBody UpdateLogisticsRequestDto requestDto) {

        return ResponseEntity.ok(logisticsRequestService.updateDraft(id, requestDto));
    }

    @PostMapping("/requests/{id}/submit")
    public ResponseEntity<LogisticsRequestResponse> submitRequest(
            @PathVariable @Positive Long id) {

        return ResponseEntity.ok(logisticsRequestService.submitRequest(id));
    }

    @PostMapping("/requests/{id}/cancel")
    public ResponseEntity<LogisticsRequestResponse> cancelRequest(
            @PathVariable @Positive Long id) {

        return ResponseEntity.ok(logisticsRequestService.cancelRequest(id));
    }
}
