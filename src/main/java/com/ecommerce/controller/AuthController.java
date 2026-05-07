package com.ecommerce.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.ecommerce.dto.LoginRequest;
import com.ecommerce.dto.RegisterRequest;
import com.ecommerce.model.User;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.security.JwtUtil;
import com.ecommerce.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;  // ← Naya

    // REGISTER API
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = userService.registerUser(request);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User registered successfully!");
            response.put("userId", user.getId());
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }

    // LOGIN API (Updated with JWT)
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest request) {
        Optional<User> userOptional = userRepository.findByUsername(request.getUsername());

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                
                // JWT Token generate karo
                String token = jwtUtil.generateToken(user.getUsername());
                
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Login successful!");
                response.put("token", token);  // ← Token bhejo
                response.put("userId", user.getId());
                response.put("username", user.getUsername());
                response.put("role", user.getRole().toString());
                
                return new ResponseEntity<>(response, HttpStatus.OK);
            }
        }
        Map<String, String> error = new HashMap<>();
        error.put("error", "Invalid username or password!");
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }
}