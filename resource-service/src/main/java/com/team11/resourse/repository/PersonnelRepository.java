package com.team11.resourse.repository;

import com.team11.resourse.entity.Personnel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface PersonnelRepository extends JpaRepository<Personnel, Long>, JpaSpecificationExecutor<Personnel> {

    Optional<Personnel> findByPersonnelCodeIgnoreCase(String personnelCode);

    boolean existsByPersonnelCodeIgnoreCase(String personnelCode);

    List<Personnel> findByUnitId(Long unitId);

    List<Personnel> findByRoleIgnoreCase(String role);

    List<Personnel> findByAvailabilityIgnoreCase(String availability);

    List<Personnel> findByActive(boolean active);
}