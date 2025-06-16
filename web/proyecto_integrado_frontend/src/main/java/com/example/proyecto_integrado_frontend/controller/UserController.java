package com.example.proyecto_integrado_frontend.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.proyecto_integrado_frontend.model.Booking;
import com.example.proyecto_integrado_frontend.model.User;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpSession;

@Controller
public class UserController {

    @GetMapping("/profile")
    public String showProfile(Model model, HttpSession session) {
        String jwt = (String) session.getAttribute("jwt");

        if (jwt == null) {
            return "redirect:/login";
        }

        try {
            String email = extractUserFromJwt(jwt).getEmail();

            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + jwt); // si tu endpoint está protegido
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<User> response = restTemplate.exchange(
                    "http://16.171.42.106:8080/api/getUser/" + email,
                    HttpMethod.GET,
                    entity,
                    User.class);

            User user = response.getBody();

            if (user != null) {
                model.addAttribute("user", user);
            } else {
                return "redirect:/login";
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/login";
        }

        return "profile";
    }

    @GetMapping("/profile/edit")
    public String showProfileForm(HttpSession session, Model model) {
        String jwt = (String) session.getAttribute("jwt");

        if (jwt == null) {
            return "redirect:/login";
        }

        try {

            String email = extractUserFromJwt(jwt).getEmail();

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + jwt);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<User> response = restTemplate.exchange(
                    "http://16.171.42.106:8080/api/getUser/" + email,
                    HttpMethod.GET,
                    entity,
                    User.class);

            User user = response.getBody();
            model.addAttribute("user", user);
            System.out.println(user);
            return "editProfile";

        } catch (Exception e) {
            return "redirect:/profile";
        }
    }

    @PostMapping("/profile/update")
    public String updateUser(@ModelAttribute("user") User user,
            @RequestParam("passwordConfirm") String passwordConfirm,
            Model model,
            HttpSession session) {
        String jwt = (String) session.getAttribute("jwt");

        if (jwt == null) {
            return "redirect:/login";
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + jwt);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            String email = extractUserFromJwt(jwt).getEmail();
            String getUrl = "http://16.171.42.106:8080/api/getUser/" + email;

            ResponseEntity<User> response = restTemplate.exchange(
                    getUrl, HttpMethod.GET, entity, User.class);

            User existingUser = response.getBody();
            user.setId(existingUser.getId());
            user.setEmail(existingUser.getEmail());
            user.setRole(existingUser.getRole());
            user.setActive(existingUser.isActive());

            if (user.getPassword() != null && !user.getPassword().equals("")) {
                if (!user.getPassword().equals(passwordConfirm)) {
                    model.addAttribute("user", user);
                    model.addAttribute("error", "Las contraseñas no coinciden.");
                    return "editProfile";
                }
                existingUser.setPassword(user.getPassword());
            } else {
                user.setPassword(existingUser.getPassword());
            }

            System.out.println(user);
            System.out.println(existingUser);

            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<User> updateEntity = new HttpEntity<>(user, headers);

            restTemplate.exchange(
                    "http://16.171.42.106:8080/api/user/update",
                    HttpMethod.POST,
                    updateEntity,
                    Void.class);

            return "redirect:/profile?success";

        } catch (Exception e) {
            model.addAttribute("error", "Error al actualizar el perfil." + e.getMessage());
            e.printStackTrace();
            return "editProfile";
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
                    "http://16.171.42.106:8080/api/getUser/" + email,
                    HttpMethod.GET,
                    entity,
                    User.class);

            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    @GetMapping("/bookingRecord")
    public String showBookingHistory(Model model, HttpSession session) {
        String jwt = (String) session.getAttribute("jwt");
        if (jwt == null) {
            return "redirect:/login";
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + jwt);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Booking[]> response = restTemplate.exchange(
                    "http://16.171.42.106:8080/api/getBookingHistory",
                    HttpMethod.GET,
                    entity,
                    Booking[].class);

            List<Booking> bookings = Arrays.asList(response.getBody());
            model.addAttribute("bookings", bookings);
            return "bookingRecord";

        } catch (Exception e) {
            model.addAttribute("error", "No se pudo obtener el historial.");
            return "bookingRecord";
        }
    }

    @GetMapping("/bookings")
    public String showBookingsPage(
            @RequestParam(required = false) String filterType,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateMin,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateMax,
            @RequestParam(required = false) String status,
            Model model,
            HttpSession session) {

        String jwt = (String) session.getAttribute("jwt");
        if (jwt == null) {
            return "redirect:/login";
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + jwt);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromHttpUrl("http://16.171.42.106:8080/api/admin/getAllBookings");

            // Aquí interpretamos correctamente los filtros
            if ("userEmail".equals(filterType) && searchTerm != null && !searchTerm.isEmpty()) {
                builder.queryParam("userEmail", searchTerm);
            } else if ("licensePlate".equals(filterType) && searchTerm != null && !searchTerm.isEmpty()) {
                builder.queryParam("licensePlate", searchTerm);
            }

            if (dateMin != null) {
                builder.queryParam("dateMin", dateMin);
            }
            if (dateMax != null) {
                builder.queryParam("dateMax", dateMax);
            }
            if (status != null && !status.isEmpty()) {
                builder.queryParam("status", status);
            }

            ResponseEntity<Booking[]> response = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    entity,
                    Booking[].class);

            List<Booking> bookings = Arrays.asList(response.getBody());
            model.addAttribute("bookings", bookings);
            model.addAttribute("filterType", filterType);
            model.addAttribute("searchTerm", searchTerm);
            model.addAttribute("dateMin", dateMin);
            model.addAttribute("dateMax", dateMax);
            model.addAttribute("status", status);

        } catch (HttpClientErrorException.NotFound e) {
            model.addAttribute("bookings", List.of());
            model.addAttribute("message", "No hay reservas registradas.");
        } catch (Exception e) {
            model.addAttribute("error", "Error al obtener las reservas.");
        }

        return "bookings";
    }

    @GetMapping("/users")
    public String listUsers(
            @RequestParam(required = false) String filterType,
            @RequestParam(required = false) String filterValue,
            HttpSession session,
            Model model) {

        String jwt = (String) session.getAttribute("jwt");
        if (jwt == null) {
            return "redirect:/login";
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + jwt);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            String url = "http://16.171.42.106:8080/api/admin/getAllClients";
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);

            // Filtrar por el tipo seleccionado
            if ("name".equalsIgnoreCase(filterType)) {
                builder.queryParam("name", filterValue);
            } else if ("email".equalsIgnoreCase(filterType)) {
                builder.queryParam("email", filterValue);
            }

            ResponseEntity<User[]> response = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    entity,
                    User[].class);

            List<User> users = Arrays.asList(response.getBody());
            model.addAttribute("users", users);
            return "users";

        } catch (Exception e) {
            model.addAttribute("error", "No se pudieron cargar los usuarios.");
            return "users";
        }
    }

}
