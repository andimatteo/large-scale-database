package it.unipi.githeritage.controller;

import it.unipi.githeritage.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody AuthRequest request) {
        boolean success = authService.register(request.username(), request.password());
        if (success) {
            return ResponseEntity.ok("User registered");
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already taken");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody AuthRequest request) {
        boolean authenticated = authService.login(request.username(), request.password());
        if (authenticated) {
            return ResponseEntity.ok("Login successful");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }
}

record AuthRequest(String username, String password) {}
