package com.team11.resourse.controller;

import com.team11.resourse.dto.PersonnelAvailabilityRequest;
import com.team11.resourse.dto.PersonnelCreateRequest;
import com.team11.resourse.dto.PersonnelResponse;
import com.team11.resourse.dto.PersonnelUpdateRequest;
import com.team11.resourse.service.PersonnelService;
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
@RequestMapping("/api/v1/personnel")
public class PersonnelController {

    private final PersonnelService personnelService;

    public PersonnelController(PersonnelService personnelService) {
        this.personnelService = personnelService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PersonnelResponse createPersonnel(@Valid @RequestBody PersonnelCreateRequest request) {
        return personnelService.createPersonnel(request);
    }

    @GetMapping
    public List<PersonnelResponse> getPersonnel(
            @RequestParam(required = false) Long unitId,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String availability,
            @RequestParam(required = false) Boolean active) {
        if (unitId == null && role == null && availability == null && active == null) {
            return personnelService.getAllPersonnel();
        }
        return personnelService.searchPersonnel(unitId, role, availability, active);
    }

    @GetMapping("/{id}")
    public PersonnelResponse getPersonnelById(@PathVariable Long id) {
        return personnelService.getPersonnelById(id);
    }

    @PutMapping("/{id}")
    public PersonnelResponse updatePersonnel(
            @PathVariable Long id,
            @Valid @RequestBody PersonnelUpdateRequest request) {
        return personnelService.updatePersonnel(id, request);
    }

    @PatchMapping("/{id}/availability")
    public PersonnelResponse updateAvailability(
            @PathVariable Long id,
            @Valid @RequestBody PersonnelAvailabilityRequest request) {
        return personnelService.updateAvailability(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePersonnel(@PathVariable Long id) {
        personnelService.deletePersonnel(id);
    }
}