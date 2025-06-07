package com.example.demo.controller;

import java.util.List;
import java.util.Map;
import com.example.demo.servicesImpl.JwtService;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    @Qualifier("bookingService")
    private BookingService bookingService;

    @Autowired
    @Qualifier("vehicleService")
    private VehicleService vehicleService;

    @Autowired
    @Qualifier("userService")
    private UserService userService;

    @Autowired
    @Qualifier("jwtService")
    private JwtService jwtService;

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

    @GetMapping("/getVehicle/{licensePlate}")
    public ResponseEntity<Vehicle> getVehicleByLicensePlate(@PathVariable String licensePlate) {
        Vehicle vehicle = vehicleService.findBylicensePlate(licensePlate);
        if (!vehicle.equals(null)) {
            return ResponseEntity.ok(vehicle);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/getUser/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        User user = userService.findByEmail(email);
        if (!user.equals(null)) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/user/update")
    public ResponseEntity<?> updateUser(@RequestBody User user) {
        
        if (user == null || user.getId() == null) {
            return ResponseEntity.badRequest().body("ID del usuario requerido");
        }

        User existingUser = userService.findById(user.getId());
    
        if (existingUser == null) {
            return ResponseEntity.notFound().build();
        }
    
        existingUser.setName(user.getName());
        existingUser.setSurname(user.getSurname());
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            existingUser.setPassword(user.getPassword());
        }
    
        User updated = userService.updateUser(existingUser);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/createBooking")
    public ResponseEntity<?> CreateBooking(@RequestBody Booking booking) {
        try {

            if (bookingService.hasConflictingBooking(booking)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Ese vehículo no esta disponible en esas fechas."));
            }

            bookingService.saveBooking(booking);

            return ResponseEntity.ok(booking);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Ha ocurrido un error inesperado. Por favor, inténtalo más tarde."));
        }

    }

    @GetMapping("/getBookingHistory")
    public ResponseEntity<?> getBookingHistory(@RequestHeader("Authorization") String token) {

        if (!jwtService.isUser(token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "No tienes permisos"));
        }

        User user = jwtService.getUser(token);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Usuario no encontrado"));
        }

        List<Booking> bookings = user.getBookings();
        if (bookings.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(bookings);

    }

    @GetMapping("/getBookings")
    public ResponseEntity<?> getUserBookings(@RequestHeader("Authorization") String token) {

        if (!jwtService.isUser(token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "No tienes permisos"));
        }

        User user = jwtService.getUser(token);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Usuario no encontrado"));
        }

        List<Booking> bookings = bookingService.getActiveBookings(user);
        if (bookings.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(bookings);

    }

    @DeleteMapping("/deleteBooking/{id}")
    public ResponseEntity<?> deleteBooking(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        try {

            User user = jwtService.getUser(token);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Usuario no encontrado"));
            }

            Booking booking = bookingService.findById(id);
            if (booking == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "No existe ninguna reserva con esos datos"));
            }

            if (booking.getUser().getId() != user.getId()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "No tiene permiso para acceder a esta reserva"));
            }

            bookingService.deleteBooking(id);

            return ResponseEntity.ok("reserva cancelada exitosamente");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Ha ocurrido un error inesperado. Por favor, inténtalo más tarde."));
        }

    }
}
