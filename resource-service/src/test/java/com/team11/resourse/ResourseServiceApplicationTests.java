package com.team11.resourse;

import com.team11.resourse.dto.VehicleCreateRequest;
import com.team11.resourse.dto.VehicleStatusRequest;
import com.team11.resourse.repository.VehicleRepository;
import com.team11.resourse.service.VehicleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:vehicle-test-db;MODE=Oracle;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
class ResourseServiceApplicationTests {

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private VehicleRepository vehicleRepository;

    @BeforeEach
    void setUp() {
        vehicleRepository.deleteAll();
    }

    @Test
    void shouldCreateVehicle() {
        var request = new VehicleCreateRequest(
            "VEH-1001",
            "Truck",
            12,
            77L,
            "HQ-01",
            "GOOD",
            "AVAILABLE"
        );

        var response = vehicleService.createVehicle(request);

        assertNotNull(response.id());
        assertEquals("VEH-1001", response.vehicleNumber());
        assertEquals("AVAILABLE", response.status());
    }

    @Test
    void shouldRejectInvalidCapacity() {
        var request = new VehicleCreateRequest(
            "VEH-1002",
            "Truck",
            0,
            77L,
            "HQ-01",
            "GOOD",
            "AVAILABLE"
        );

        var exception = assertThrows(ResponseStatusException.class, () -> vehicleService.createVehicle(request));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void shouldRejectInvalidStatus() {
        var request = new VehicleCreateRequest(
            "VEH-1003",
            "Truck",
            8,
            77L,
            "HQ-01",
            "GOOD",
            "BROKEN"
        );

        var exception = assertThrows(ResponseStatusException.class, () -> vehicleService.createVehicle(request));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void shouldRejectDuplicateVehicleNumber() {
        var request = new VehicleCreateRequest(
            "VEH-1004",
            "Truck",
            8,
            77L,
            "HQ-01",
            "GOOD",
            "AVAILABLE"
        );

        vehicleService.createVehicle(request);

        var exception = assertThrows(ResponseStatusException.class, () -> vehicleService.createVehicle(request));
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    @Test
    void shouldReturn404ForMissingVehicle() {
        var exception = assertThrows(ResponseStatusException.class, () -> vehicleService.getVehicleById(9999L));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void shouldUpdateVehicleStatus() {
        var request = new VehicleCreateRequest(
            "VEH-1005",
            "Truck",
            8,
            77L,
            "HQ-01",
            "GOOD",
            "AVAILABLE"
        );

        var created = vehicleService.createVehicle(request);
        var updated = vehicleService.updateVehicleStatus(created.id(), new VehicleStatusRequest("IN_USE"));

        assertEquals("IN_USE", updated.status());
    }

    @Test
    void shouldDeleteVehicle() {
        var request = new VehicleCreateRequest(
            "VEH-1006",
            "Truck",
            8,
            77L,
            "HQ-01",
            "GOOD",
            "AVAILABLE"
        );

        var created = vehicleService.createVehicle(request);
        vehicleService.deleteVehicle(created.id());

        var exception = assertThrows(ResponseStatusException.class, () -> vehicleService.getVehicleById(created.id()));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }
}