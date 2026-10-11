package com.team11.defense_logistics.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PersonnelDto {

    private Long id;

    @NotBlank(message = "Personnel code is required")
    @Size(max = 50, message = "Personnel code must not exceed 50 characters")
    private String personnelCode;

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "Role is required")
    @Size(max = 100, message = "Role must not exceed 100 characters")
    private String role;

    @NotBlank(message = "Unit is required")
    @Size(max = 100, message = "Unit must not exceed 100 characters")
    private String unit;

    @NotBlank(message = "Posting is required")
    @Size(max = 150, message = "Posting must not exceed 150 characters")
    private String posting;

    @NotBlank(message = "Availability is required")
    private String availability = "AVAILABLE";

    private boolean active = true;

    public PersonnelDto() {
    }

    public PersonnelDto(
            Long id,
            String personnelCode,
            String name,
            String role,
            String unit,
            String posting,
            String availability,
            boolean active) {

        this.id = id;
        this.personnelCode = personnelCode;
        this.name = name;
        this.role = role;
        this.unit = unit;
        this.posting = posting;
        this.availability = availability;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPersonnelCode() {
        return personnelCode;
    }

    public void setPersonnelCode(String personnelCode) {
        this.personnelCode = personnelCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getPosting() {
        return posting;
    }

    public void setPosting(String posting) {
        this.posting = posting;
    }

    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
