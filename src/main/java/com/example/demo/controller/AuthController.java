package com.example.demo.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.services.UserService;
import com.example.demo.servicesImpl.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
	@Autowired
	@Qualifier("userAuthService")
	private AuthService authService;

	@Autowired
	@Qualifier("userService")
	private UserService userService;

    @PostMapping("/register")
	public ResponseEntity<?> register(@RequestBody User user) {
		try {

			if (userService.existsByEmail(user.getEmail())) {
				return ResponseEntity.badRequest().body(Map.of("error", "El correo electrónico ya está en uso."));
			}

			String jwt = authService.register(user);

			return ResponseEntity.ok(Map.of(
					"message", "Usuario registrado exitosamente.",
					"userId", user.getId(),
					"jwt", jwt
			));
		} catch (RuntimeException rte) {
			return ResponseEntity.badRequest().body(Map.of("error", "No se pudo completar el registro: " + rte.getMessage()));
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("error", "Ha ocurrido un error inesperado. Por favor, inténtalo más tarde."));
		}
	}


	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody Map<String, String> loginData) {
		try {
			String email = loginData.get("email");
			String password = loginData.get("password");

			String jwt = authService.login(email, password);
			User user = userService.findByEmail(email);
			Role role = user.getRole();
			String nombre = user.getName();

			return ResponseEntity.ok(Map.of(
				"jwt", jwt,
				"role", role,
					"nombre", nombre
			));
		} catch (BadCredentialsException bcex) {
			return ResponseEntity.badRequest().body(Map.of("error", "Correo o contraseña incorrectos. Verifica tus datos."));
		} catch (AuthenticationException aex) {
			return ResponseEntity.badRequest().body(Map.of("error", "Error en la autenticación. Verifica tus credenciales."));
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("error", "Ha ocurrido un problema inesperado."));
		}
	}
}
