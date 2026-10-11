
package com.team11.resourse.repository;

import com.team11.resourse.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long>, JpaSpecificationExecutor<Vehicle> {

    Optional<Vehicle> findByVehicleNumber(String vehicleNumber);

    Optional<Vehicle> findByVehicleNumberIgnoreCase(String vehicleNumber);

    boolean existsByVehicleNumber(String vehicleNumber);

    boolean existsByVehicleNumberIgnoreCase(String vehicleNumber);

    List<Vehicle> findByUnitId(Long unitId);

    List<Vehicle> findByStatus(String status);

    List<Vehicle> findByVehicleType(String vehicleType);
}
