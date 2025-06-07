package com.example.demo.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.Booking;
import com.example.demo.entity.User;
import com.example.demo.entity.Vehicle;
import com.example.demo.entity.VehicleType;
import com.example.demo.services.BookingService;
import com.example.demo.services.UserService;
import com.example.demo.services.VehicleService;

import jakarta.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    @Qualifier("vehicleService")
    private VehicleService vehicleService;

    @Autowired
    @Qualifier("userService")
    private UserService userService;

    @Autowired
    @Qualifier("bookingService")
    private BookingService bookingService;

    // hacer crud vehiculos

    @PostMapping("/registerVehicle")
    public ResponseEntity<?> createVeahicle(@RequestBody Vehicle vehicle) {
        try {

            if (vehicleService.existByLicensePlate(vehicle.getLicensePlate())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Ya existe un vehiculo con esos credenciales"));
            }

            vehicleService.saveVehicle(vehicle);

            return ResponseEntity.ok(vehicle);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Ha ocurrido un error inesperado. Por favor, inténtalo más tarde."));
        }

    }

    @DeleteMapping("/deleteVehicle/{licensePlate}")
    public ResponseEntity<?> deleteVeahicle(@PathVariable  String licensePlate) {
        try {

            Vehicle vehicle = vehicleService.findBylicensePlate(licensePlate);
            if (vehicle == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "No existe ningun vehículo con esos datos: " + vehicle));
            }

            vehicleService.deleteVehicle(licensePlate);

            return ResponseEntity.ok("vehiculo eliminado exitosamente");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Ha ocurrido un error inesperado. Por favor, inténtalo más tarde."));
        }

    }

    @PutMapping("/updateVehicle")
    public ResponseEntity<?> updateVehicle(@RequestBody Vehicle vehicle) {
        try {

            Vehicle existingVehicle = vehicleService.findBylicensePlate(vehicle.getLicensePlate());

            if (existingVehicle == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error",
                                "No se encontró un vehículo con la matrícula: " + vehicle.getLicensePlate()));
            }

            existingVehicle.setBrand(vehicle.getBrand());
            existingVehicle.setModel(vehicle.getModel());
            existingVehicle.setYear(vehicle.getYear());
            existingVehicle.setRentalPricePerDay(vehicle.getRentalPricePerDay());
            existingVehicle.setAvailable(vehicle.isAvailable());
            existingVehicle.setType(vehicle.getType());

            vehicleService.saveVehicle(existingVehicle);

            return ResponseEntity.ok(Map.of("message", "Vehículo actualizado exitosamente"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Ocurrió un error inesperado al actualizar el vehículo."));
        }
    }

    @GetMapping("/getAllVehicles")
    public ResponseEntity<?> getAllVehicles(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) Integer yearMin,
            @RequestParam(required = false) Integer yearMax,
            @RequestParam(required = false) Boolean available,
            @RequestParam(required = false) VehicleType type) {

        try {
            List<Vehicle> vehicles = vehicleService.findVehicles(brand, model, yearMin, yearMax, available, type);

            if (vehicles.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "No hay vehículos registrados."));
            }

            return ResponseEntity.ok(vehicles);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Ocurrió un error inesperado al obtener los vehículos."));
        }
    }

    @PutMapping("/toggleAvailability/{licensePlate}")
    public ResponseEntity<?> toggleAvailability(@PathVariable String licensePlate) {
        try {
            vehicleService.toggleAvailable(licensePlate);
            return ResponseEntity.ok(Map.of("message", "Disponibilidad actualizada correctamente"));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Ha ocurrido un error inesperado."));
        }
    }


    @GetMapping("/getAllClients")
    public ResponseEntity<?> getAllClients(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email) {

        try {
            List<User> clients = userService.getAllClients(name, email);

            if (clients.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "No hay clientes registrados."));
            }

            return ResponseEntity.ok(clients);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Ocurrió un error inesperado al obtener los clientes."));
        }
    }

    @GetMapping("/getAllBookings")
    public ResponseEntity<?> getAllBookings(
            @RequestParam(required = false) String userEmail,
            @RequestParam(required = false) String licensePlate,
            @RequestParam(required = false) LocalDate dateMin,
            @RequestParam(required = false) LocalDate dateMax,
            @RequestParam(required = false) String status) {

        try {
            List<Booking> bookings = bookingService.findBookings(userEmail, licensePlate, dateMin, dateMax, status);

            if (bookings.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "No hay prestamos registrados."));
            }

            return ResponseEntity.ok(bookings);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Ocurrió un error inesperado al obtener los prestamos."));
        }
    }
}
