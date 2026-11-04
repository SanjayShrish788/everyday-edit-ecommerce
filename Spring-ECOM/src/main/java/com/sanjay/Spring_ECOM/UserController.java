package com.sanjay.Spring_ECOM;

import com.sanjay.Spring_ECOM.model.User;
import com.sanjay.Spring_ECOM.repo.UserRepo;
import com.sanjay.Spring_ECOM.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController

public class UserController {
    @org.springframework.beans.factory.annotation.Value("${app.admin-username}") private String admin;
    private final UserRepo users;
    private final UserService service;

    public UserController(UserRepo users, UserService service) {
        this.users = users;
        this.service = service;
    }

    /** Creates an account without exposing its password hash in the response. */
    @PostMapping("/api/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody User user) {
        String username = user.getUsername() == null ? "" : user.getUsername().trim();
        String password = user.getPassword();
        // BCrypt limits input to 72 bytes, which may be fewer than 72 Unicode characters.
        if (!username.matches("[A-Za-z0-9_.-]{3,40}") || password == null
                || password.length() < 8 || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid username or password"));
        }
        if (username.equals(admin) || users.findByUsername(username) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Username already exists"));
        }
        user.setId(null);
        user.setUsername(username);
        // UserService hashes the password; return only the public username.
        try { service.saveUser(user); }
        catch (org.springframework.dao.DataIntegrityViolationException duplicate) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Username already exists"));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("username", username));
    }
}
