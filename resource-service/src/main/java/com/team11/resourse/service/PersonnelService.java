package com.team11.resourse.service;

import com.team11.resourse.dto.PersonnelAvailabilityRequest;
import com.team11.resourse.dto.PersonnelCreateRequest;
import com.team11.resourse.dto.PersonnelResponse;
import com.team11.resourse.dto.PersonnelUpdateRequest;
import com.team11.resourse.entity.Personnel;
import com.team11.resourse.repository.PersonnelRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class PersonnelService {

    private final PersonnelRepository personnelRepository;

    public PersonnelService(PersonnelRepository personnelRepository) {
        this.personnelRepository = personnelRepository;
    }

    public PersonnelResponse createPersonnel(PersonnelCreateRequest request) {
        validateRequiredFields(request == null ? null : request.personnelCode(),
                request == null ? null : request.fullName(),
                request == null ? null : request.role(),
                request == null ? null : request.unitId());

        String personnelCode = normalize(request.personnelCode());
        ensureCodeAvailable(personnelCode);

        Personnel personnel = new Personnel();
        personnel.setPersonnelCode(personnelCode);
        personnel.setFullName(normalize(request.fullName()));
        personnel.setRole(normalize(request.role()));
        personnel.setUnitId(request.unitId());
        personnel.setPostingLocation(normalize(request.postingLocation()));
        personnel.setAvailability(isBlank(request.availability()) ? "AVAILABLE" : normalize(request.availability()));
        personnel.setActive(request.active() == null || request.active());

        return toResponse(personnelRepository.save(personnel));
    }

    @Transactional(readOnly = true)
    public List<PersonnelResponse> getAllPersonnel() {
        return personnelRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<PersonnelResponse> searchPersonnel(Long unitId, String role, String availability, Boolean active) {
        Specification<Personnel> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (unitId != null) {
                predicates.add(criteriaBuilder.equal(root.get("unitId"), unitId));
            }
            if (!isBlank(role)) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.lower(root.get("role")), normalize(role).toLowerCase(Locale.ROOT)));
            }
            if (!isBlank(availability)) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.lower(root.get("availability")), normalize(availability).toLowerCase(Locale.ROOT)));
            }
            if (active != null) {
                predicates.add(criteriaBuilder.equal(root.get("active"), active));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return personnelRepository.findAll(specification).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PersonnelResponse getPersonnelById(Long id) {
        return toResponse(findPersonnel(id));
    }

    public PersonnelResponse updatePersonnel(Long id, PersonnelUpdateRequest request) {
        validateRequiredFields(request == null ? null : request.personnelCode(),
                request == null ? null : request.fullName(),
                request == null ? null : request.role(),
                request == null ? null : request.unitId());

        Personnel personnel = findPersonnel(id);
        String personnelCode = normalize(request.personnelCode());
        if (!personnel.getPersonnelCode().equalsIgnoreCase(personnelCode)) {
            ensureCodeAvailable(personnelCode);
        }

        personnel.setPersonnelCode(personnelCode);
        personnel.setFullName(normalize(request.fullName()));
        personnel.setRole(normalize(request.role()));
        personnel.setUnitId(request.unitId());
        personnel.setPostingLocation(normalize(request.postingLocation()));
        if (!isBlank(request.availability())) {
            personnel.setAvailability(normalize(request.availability()));
        }
        if (request.active() != null) {
            personnel.setActive(request.active());
        }

        return toResponse(personnelRepository.save(personnel));
    }

    public PersonnelResponse updateAvailability(Long id, PersonnelAvailabilityRequest request) {
        if (request == null || isBlank(request.availability())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Availability is required");
        }

        Personnel personnel = findPersonnel(id);
        personnel.setAvailability(normalize(request.availability()));
        return toResponse(personnelRepository.save(personnel));
    }

    public void deletePersonnel(Long id) {
        personnelRepository.delete(findPersonnel(id));
    }

    private Personnel findPersonnel(Long id) {
        return personnelRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Personnel not found with ID: " + id));
    }

    private void ensureCodeAvailable(String personnelCode) {
        if (personnelRepository.existsByPersonnelCodeIgnoreCase(personnelCode)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Personnel code already exists");
        }
    }

    private void validateRequiredFields(String personnelCode, String fullName, String role, Long unitId) {
        if (isBlank(personnelCode)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Personnel code is required");
        }
        if (isBlank(fullName)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Full name is required");
        }
        if (isBlank(role)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role is required");
        }
        if (unitId == null || unitId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unit ID must be greater than zero");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private PersonnelResponse toResponse(Personnel personnel) {
        return new PersonnelResponse(
                personnel.getPersonnelId(),
                personnel.getPersonnelCode(),
                personnel.getFullName(),
                personnel.getRole(),
                personnel.getUnitId(),
                personnel.getPostingLocation(),
                personnel.getAvailability(),
                personnel.isActive(),
                personnel.getCreatedAt(),
                personnel.getUpdatedAt());
    }
}