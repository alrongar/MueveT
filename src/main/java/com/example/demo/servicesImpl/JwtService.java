package com.example.demo.servicesImpl;

import com.example.demo.entity.User;
import com.example.demo.services.UserService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private static final long EXPIRATION_HOURS = 24;
    private static final long EXPIRATION = EXPIRATION_HOURS * 60 * 60 * 1000;
    private static final String SECRET_STRING = "4765638448765djwalcottmejordjdecadiz234q567r84950434q3864hvntfhy5y5gt5";
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(SECRET_STRING.getBytes(StandardCharsets.UTF_8));

    @Autowired
    private UserService userService;

    public String generateToken(UserDetails user) {
        return Jwts.builder()
                .subject(user.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(SECRET_KEY)
                .compact();
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public User getUser(String token) {
        return userService.findByEmail(extractUsername(cleanToken(token)));
    }

    public String extractUsername(String token) {
        return getClaims(cleanToken(token)).getSubject();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return getClaims(cleanToken(token)).getExpiration().before(new Date());
    }

    public boolean isAdmin(String token) {
        return hasRole(token, "ROLE_ADMIN");
    }

    public boolean isUser(String token) {
        return hasRole(token, "ROLE_USER");
    }

    private boolean hasRole(String token, String role) {
        return getUser(token).getRole().name().equals(role);
    }

    private String cleanToken(String token) {
        return token.replace("Bearer ", "");
    }
}