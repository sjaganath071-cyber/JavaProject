
package com.team11.logistics.service;

import com.team11.logistics.dto.VehicleRequest;
import com.team11.logistics.entity.Vehicle;
import com.team11.logistics.repository.VehicleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    public VehicleService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @Transactional
    public Vehicle createVehicle(VehicleRequest request) {
        String registrationNumber = request.getRegistrationNumber().trim();

        if (vehicleRepository.existsByRegistrationNumber(registrationNumber)) {
            throw new IllegalStateException(
                    "A vehicle with this registration number already exists");
        }

        Vehicle vehicle = new Vehicle();
        copyRequestToVehicle(request, vehicle);

        return vehicleRepository.save(vehicle);
    }

    @Transactional(readOnly = true)
    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Vehicle getVehicleById(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Vehicle not found with ID: " + id));
    }

    @Transactional
    public Vehicle updateVehicle(Long id, VehicleRequest request) {
        Vehicle vehicle = getVehicleById(id);
        String registrationNumber = request.getRegistrationNumber().trim();

        if (vehicleRepository.existsByRegistrationNumberAndIdNot(
                registrationNumber, id)) {
            throw new IllegalStateException(
                    "Another vehicle already uses this registration number");
        }

        copyRequestToVehicle(request, vehicle);

        return vehicleRepository.save(vehicle);
    }

    @Transactional
    public void deleteVehicle(Long id) {
        Vehicle vehicle = getVehicleById(id);
        vehicleRepository.delete(vehicle);
    }

    private void copyRequestToVehicle(
            VehicleRequest request, Vehicle vehicle) {

        vehicle.setVehicleName(request.getVehicleName().trim());
        vehicle.setVehicleType(request.getVehicleType().trim());
        vehicle.setRegistrationNumber(
                request.getRegistrationNumber().trim());
        vehicle.setStatus(request.getStatus().trim());
    }
}
