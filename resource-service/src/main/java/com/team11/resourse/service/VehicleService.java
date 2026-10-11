
package com.team11.resourse.service;

import com.team11.resourse.dto.VehicleCreateRequest;
import com.team11.resourse.dto.VehicleResponse;
import com.team11.resourse.dto.VehicleStatusRequest;
import com.team11.resourse.entity.Vehicle;
import com.team11.resourse.repository.VehicleRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.http.HttpStatus;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class VehicleService {

    private static final String VALID_STATUS_PATTERN = "^(AVAILABLE|RESERVED|ASSIGNED|IN_USE|UNDER_MAINTENANCE|UNAVAILABLE)$";

    private final VehicleRepository vehicleRepository;

    public VehicleService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    public VehicleResponse createVehicle(VehicleCreateRequest request) {
        validateCreateRequest(request);

        String vehicleNumber = normalizeText(request.vehicleNumber());
        String vehicleType = normalizeText(request.vehicleType());
        String condition = normalizeText(request.condition());
        String status = normalizeStatus(request.status());

        validateStatus(status);

        if (vehicleRepository.existsByVehicleNumberIgnoreCase(vehicleNumber)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Vehicle number already exists");
        }

        Vehicle vehicle = new Vehicle();
        vehicle.setVehicleNumber(vehicleNumber);
        vehicle.setVehicleType(vehicleType);
        vehicle.setCapacity(request.capacity());
        vehicle.setUnitId(request.unitId());
        vehicle.setLocationCode(normalizeText(request.locationCode()));
        vehicle.setCondition(condition);
        vehicle.setStatus(status);

        return toResponse(vehicleRepository.save(vehicle));
    }

    @Transactional(readOnly = true)
    public List<VehicleResponse> getAllVehicles() {
        return vehicleRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<VehicleResponse> getAllVehicles(Long unitId, String vehicleType, String locationCode, String status) {
        Specification<Vehicle> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (unitId != null) {
                predicates.add(criteriaBuilder.equal(root.get("unitId"), unitId));
            }

            if (!isBlank(vehicleType)) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.lower(root.get("vehicleType")),
                        normalizeText(vehicleType).toLowerCase(Locale.ROOT)
                ));
            }

            if (!isBlank(locationCode)) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.lower(root.get("locationCode")),
                        normalizeText(locationCode).toLowerCase(Locale.ROOT)
                ));
            }

            if (!isBlank(status)) {
                String normalizedStatus = normalizeStatus(status);
                validateStatus(normalizedStatus);
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.lower(root.get("status")),
                        normalizedStatus.toLowerCase(Locale.ROOT)
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return vehicleRepository.findAll(specification)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<VehicleResponse> getAvailableVehicles(LocalDateTime from, LocalDateTime until, Long unitId) {
        if (from == null || until == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "from and until are required");
        }

        if (!from.isBefore(until)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "from must be earlier than until");
        }

        Specification<Vehicle> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("status")), "available"));

            if (unitId != null) {
                predicates.add(criteriaBuilder.equal(root.get("unitId"), unitId));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return vehicleRepository.findAll(specification)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public VehicleResponse getVehicleById(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Vehicle not found with ID: " + id
                ));

        return toResponse(vehicle);
    }

    public VehicleResponse updateVehicleStatus(Long id, VehicleStatusRequest request) {
        if (request == null || isBlank(request.status())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status is required");
        }

        String status = normalizeStatus(request.status());
        validateStatus(status);

        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Vehicle not found with ID: " + id
                ));

        vehicle.setStatus(status);
        return toResponse(vehicleRepository.save(vehicle));
    }

    public VehicleResponse updateVehicle(Long id, VehicleCreateRequest request) {
        validateCreateRequest(request);

        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Vehicle not found with ID: " + id
                ));

        String newVehicleNumber = normalizeText(request.vehicleNumber());
        String status = normalizeStatus(request.status());

        validateStatus(status);

        if (!vehicle.getVehicleNumber().equalsIgnoreCase(newVehicleNumber)
                && vehicleRepository.existsByVehicleNumberIgnoreCase(newVehicleNumber)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Vehicle number already exists");
        }

        vehicle.setVehicleNumber(newVehicleNumber);
        vehicle.setVehicleType(normalizeText(request.vehicleType()));
        vehicle.setCapacity(request.capacity());
        vehicle.setUnitId(request.unitId());
        vehicle.setLocationCode(normalizeText(request.locationCode()));
        vehicle.setCondition(normalizeText(request.condition()));
        vehicle.setStatus(status);

        return toResponse(vehicleRepository.save(vehicle));
    }

    public void deleteVehicle(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Vehicle not found with ID: " + id
                ));

        vehicleRepository.delete(vehicle);
    }

    private void validateCreateRequest(VehicleCreateRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vehicle request is required");
        }

        if (isBlank(request.vehicleNumber())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vehicle number is required");
        }

        if (isBlank(request.vehicleType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vehicle type is required");
        }

        if (request.capacity() == null || request.capacity() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Capacity must be greater than zero");
        }

        if (isBlank(request.condition())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Condition is required");
        }

        if (isBlank(request.status())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status is required");
        }
    }

    private void validateStatus(String status) {
        if (status == null || !status.matches(VALID_STATUS_PATTERN)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid vehicle status");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String normalizeStatus(String value) {
        return value == null ? null : value.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeText(String value) {
        return value == null ? null : value.trim();
    }

    private VehicleResponse toResponse(Vehicle vehicle) {
        return new VehicleResponse(
                vehicle.getId(),
                vehicle.getVehicleNumber(),
                vehicle.getVehicleType(),
                vehicle.getCapacity(),
                vehicle.getUnitId(),
                vehicle.getLocationCode(),
                vehicle.getCondition(),
                vehicle.getStatus(),
                vehicle.getCreatedAt(),
                vehicle.getUpdatedAt()
        );
    }
}
