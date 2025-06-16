package com.example.proyecto_integrado_frontend.controller;

import java.util.Arrays;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

import com.example.proyecto_integrado_frontend.model.Booking;

import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {

    @GetMapping("/home")
    public String homePage(HttpSession session, Model model) {
        String role = session.getAttribute("role").toString();
        model.addAttribute("name", session.getAttribute("name"));
        model.addAttribute("role", role);

        String jwt = (String) session.getAttribute("jwt");
        if (role.equals("ROLE_USER")) {

            if (jwt != null) {
                try {
                    RestTemplate restTemplate = new RestTemplate();
                    HttpHeaders headers = new HttpHeaders();
                    headers.set("Authorization", "Bearer " + jwt);

                    HttpEntity<String> entity = new HttpEntity<>(headers);

                    ResponseEntity<Booking[]> response = restTemplate.exchange(
                            "http://16.171.42.106:8080/api/getBookings",
                            HttpMethod.GET,
                            entity,
                            Booking[].class);
                    System.out.println(response);
                    if (response.getStatusCode() == HttpStatus.OK) {
                        Booking[] bookings = response.getBody();
                        model.addAttribute("bookings", Arrays.asList(bookings));

                    } else {
                        model.addAttribute("bookings", new Booking[0]);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    model.addAttribute("bookings", new Booking[0]);
                }
            } else {
                model.addAttribute("bookings", new Booking[0]);
            }
        }
        return "home";
    }
}
