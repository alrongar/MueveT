package com.example.demo.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
    
    @Autowired
    @Qualifier("filtroJWT")
    private FiltroJWT jFiltro;

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Rutas públicas (Web + API)
                        .requestMatchers("/", "/imgs/**", "/auth/**", "/register", "/contact", "/profile/**")
                        .permitAll()
                        .requestMatchers("/api/auth/**").permitAll()

                        // Seguridad API
                        .requestMatchers("/api/admin/**").hasRole("ADMIN") 

                        // Rutas autenticadas
                        .requestMatchers("/home").authenticated()

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form.disable())
                .logout(logout -> logout.disable())
                .addFilterBefore(jFiltro, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
