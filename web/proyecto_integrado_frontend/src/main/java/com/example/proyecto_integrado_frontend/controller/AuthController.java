package com.example.proyecto_integrado_frontend.controller;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.example.proyecto_integrado_frontend.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.Map;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
            Model model) {
        if (error != null) {
            model.addAttribute("error", "Credenciales incorrectas o cuenta inactiva");
        }
        return "/login"; // Retorna la vista login.html
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam("email") String email,
            @RequestParam("password") String password,
            Model model,
            HttpSession session) {

        Map<String, String> loginData = new HashMap<>();
        loginData.put("email", email);
        loginData.put("password", password);

        try {
            RestTemplate restTemplate = new RestTemplate();

            String apiUrl = "http://localhost:8080/api/auth/login";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(loginData, headers);

            ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, Map.class);

            // Verificar si la respuesta es exitosa
            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();

                // Si el backend devuelve un JWT, guardar los datos en la sesión
                String jwt = (String) responseBody.get("jwt");
                String name = (String) responseBody.get("nombre");
                String role = (String) responseBody.get("role");

                // Guardar en la sesión
                session.setAttribute("jwt", jwt);
                session.setAttribute("name", name);
                session.setAttribute("role", role);

                // Redirigir al home
                return "redirect:/home";
            } else {
                model.addAttribute("error", "Error al iniciar sesión");
            }

        } catch (HttpClientErrorException ex) {
            // Manejar errores de la solicitud
            ex.printStackTrace();
            model.addAttribute("error", "Error en la autenticación. Verifica tus credenciales.");
        }

        // Si hubo algún error, volver al formulario de login
        return "/login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(
            @ModelAttribute("user") User user,
            Model model) {

        System.out.println(user);
        if (!user.getPassword().equals(user.getConfirmPassword())) {
            model.addAttribute("error", "Las contraseñas no coinciden.");
            return "register";
        }

        try {
            
            RestTemplate restTemplate = new RestTemplate();
            String apiUrl = "http://localhost:8080/api/auth/register"; 
            Map<String, Object> userData = Map.of(
                    "email", user.getEmail(),
                    "name", user.getName(),
                    "surname", user.getSurname(),
                    "role", user.getRole(),
                    "active", user.isActive(),
                    "password", user.getPassword());

            restTemplate.postForObject(apiUrl, userData, Void.class);

            return "redirect:/login"; 

        } catch (HttpClientErrorException ex) {
            model.addAttribute("error", "No se pudo registrar el usuario: " + ex.getResponseBodyAsString());
            return "register";
        }
    }

}
