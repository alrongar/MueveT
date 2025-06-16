package com.example.proyecto_integrado_frontend.controller;

import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.proyecto_integrado_frontend.model.Booking;
import com.example.proyecto_integrado_frontend.model.User;
import com.example.proyecto_integrado_frontend.model.Vehicle;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpSession;

@Controller
public class VehicleController {

    @GetMapping("/vehicles")
    public String showVehiclesPage(
            @RequestParam(required = false) String searchField,
            @RequestParam(required = false) String searchValue,
            @RequestParam(required = false) Integer yearMin,
            @RequestParam(required = false) Integer yearMax,
            @RequestParam(required = false) Boolean available,
            Model model,
            HttpSession session) {

        String jwt = (String) session.getAttribute("jwt");
        if (jwt == null)
            return "redirect:/login";

        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + jwt);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            User user = extractUserFromJwt(jwt);
            String role = user.getRole();

            // Construcción de URI con filtros
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromHttpUrl("http://localhost:8080/api/getAllVehicles");

            if (searchValue != null && !searchValue.isBlank()) {
                if ("brand".equals(searchField)) {
                    builder.queryParam("brand", searchValue);
                } else if ("model".equals(searchField)) {
                    builder.queryParam("model", searchValue);
                } else if ("type".equals(searchField)) {
                    builder.queryParam("type", searchValue);
                }
            }

            if (yearMin != null)
                builder.queryParam("yearMin", yearMin);

            if (yearMax != null)
                builder.queryParam("yearMax", yearMax);
            if ("ROLE_ADMIN".equals(role)) {
                
                if (available != null) {
                    builder.queryParam("available", true);
                }
            } else {
                
                builder.queryParam("available", true);
            }

            ResponseEntity<Vehicle[]> response = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    entity,
                    Vehicle[].class);

            model.addAttribute("role", user.getRole());
            List<Vehicle> vehicles = Arrays.asList(response.getBody());
            model.addAttribute("vehicles", vehicles);
            return "vehicles";

        } catch (Exception e) {
            model.addAttribute("error", "No se pudieron obtener los vehículos.");
            return "vehicles";
        }
    }

    @GetMapping("/booking/form/{licensePlate}")
    public String showBookingForm(@PathVariable String licensePlate, Model model, HttpSession session) {
        String jwt = (String) session.getAttribute("jwt");

        if (jwt == null) {
            return "redirect:/login";
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + jwt);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String apiUrl = "http://localhost:8080/api/getVehicle/" + licensePlate;

            ResponseEntity<Vehicle> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.GET,
                    entity,
                    Vehicle.class);

            model.addAttribute("vehicle", response.getBody());
            model.addAttribute("booking", new Booking());

            return "booking-form";

        } catch (Exception e) {
            return "redirect:/vehicles";
        }
    }

    @PostMapping("/booking/submit")
    public String submitBooking(@ModelAttribute Booking booking, HttpSession session) {
        String jwt = (String) session.getAttribute("jwt");
        if (jwt == null) {
            return "redirect:/login";
        }

        try {

            String email = extractUserFromJwt(jwt).getEmail();

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + jwt);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<User> response = restTemplate.exchange(
                    "http://localhost:8080/api/getUser/" + email,
                    HttpMethod.GET,
                    entity,
                    User.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                User user = response.getBody();
                booking.setUser(user);
                booking.setStatus("ACTIVE");

                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Booking> bookingEntity = new HttpEntity<>(booking, headers);

                restTemplate.exchange(
                        "http://localhost:8080/api/createBooking",
                        HttpMethod.POST,
                        bookingEntity,
                        Void.class);

                return "redirect:/home";
            } else {
                return "redirect:/vehicles";
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/vehicles";
        }
    }

    @PostMapping("/cancelBooking")
    public String cancelBooking(@RequestParam Long bookingId, HttpSession session) {
        String jwt = (String) session.getAttribute("jwt");
        if (jwt == null) {
            return "redirect:/login";
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + jwt);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            restTemplate.exchange(
                    "http://localhost:8080/api/deleteBooking/" + bookingId,
                    HttpMethod.DELETE,
                    entity,
                    Void.class);

            return "redirect:/home";

        } catch (Exception e) {
            return "redirect:/home?error=true";
        }
    }

    @GetMapping("/vehicleDetails/{licensePlate}")
    public String showVehicleDetails(@PathVariable String licensePlate, Model model, HttpSession session) {
        String jwt = (String) session.getAttribute("jwt");
        if (jwt == null) {
            return "redirect:/login";
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + jwt);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Vehicle> response = restTemplate.exchange(
                    "http://localhost:8080/api/getVehicle/" + licensePlate,
                    HttpMethod.GET,
                    entity,
                    Vehicle.class);

            model.addAttribute("vehicle", response.getBody());
            model.addAttribute("role", extractUserFromJwt(jwt).getRole());
            return "vehicleDetails";

        } catch (Exception e) {
            return "redirect:/vehicles";
        }
    }

    private User extractUserFromJwt(String jwt) {

        try {
            String[] chunks = jwt.split("\\.");
            String payload = new String(Base64.getUrlDecoder().decode(chunks[1]));
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(payload);
            String email = node.get("sub").asText();

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + jwt);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<User> response = restTemplate.exchange(
                    "http://localhost:8080/api/getUser/" + email,
                    HttpMethod.GET,
                    entity,
                    User.class);

            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    @GetMapping("/vehicles/create")
    public String showCreateVehicleForm(HttpSession session, Model model) {
        String jwt = (String) session.getAttribute("jwt");
        if (jwt == null)
            return "redirect:/login";

        User user = extractUserFromJwt(jwt);
        if (!user.getRole().equals("ROLE_ADMIN"))
            return "redirect:/vehicles";

        model.addAttribute("vehicle", new Vehicle());
        return "createVehicle";
    }

    @PostMapping("/vehicles/create")
    public String createVehicle(@ModelAttribute Vehicle vehicle, HttpSession session, Model model) {
        String jwt = (String) session.getAttribute("jwt");
        if (jwt == null)
            return "redirect:/login";

        try {
            System.out.println("Enviando vehículo: " + vehicle.getBrand());
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + jwt);

            HttpEntity<Vehicle> request = new HttpEntity<>(vehicle, headers);

            restTemplate.postForEntity("http://localhost:8080/api/admin/registerVehicle", request, Vehicle.class);

            return "redirect:/vehicles?success";

        } catch (Exception e) {
            model.addAttribute("error", "Error al crear el vehículo.");
            model.addAttribute("vehicle", vehicle);
            return "createVehicle";
        }
    }

    @GetMapping("/vehicles/edit/{licensePlate}")
    public String showEditVehicleForm(@PathVariable String licensePlate, Model model, HttpSession session) {
        String jwt = (String) session.getAttribute("jwt");
        if (jwt == null) {
            return "redirect:/login";
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + jwt);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Vehicle> response = restTemplate.exchange(
                    "http://localhost:8080/api/getVehicle/" + licensePlate,
                    HttpMethod.GET,
                    entity,
                    Vehicle.class);

            model.addAttribute("vehicle", response.getBody());
            return "editVehicle";
        } catch (Exception e) {
            return "redirect:/vehicles";
        }
    }

    @PostMapping("/vehicles/update")
    public String updateVehicle(@ModelAttribute Vehicle vehicle, HttpSession session) {
        String jwt = (String) session.getAttribute("jwt");
        if (jwt == null) {
            return "redirect:/login";
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + jwt);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Vehicle> request = new HttpEntity<>(vehicle, headers);

            restTemplate.exchange(
                    "http://localhost:8080/api/admin/updateVehicle",
                    HttpMethod.PUT,
                    request,
                    Void.class);

            return "redirect:/vehicles";
        } catch (Exception e) {
            return "redirect:/vehicles?error";
        }
    }

}