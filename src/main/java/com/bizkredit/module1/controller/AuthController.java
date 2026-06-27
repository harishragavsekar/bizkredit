package com.bizkredit.module1.controller;
import com.bizkredit.module1.dto.LoginRequest;
import com.bizkredit.module1.dto.RegisterRequest;
import com.bizkredit.module1.dto.AuthResponse;
import com.bizkredit.common.dto.ApiResponse;


import com.bizkredit.module1.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Module 1: Auth, Users, Scope & Audit")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("User registered successfully", authService.register(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Login successful", authService.login(request)));
    }

    // Stateless logout - client discards token; server logs the action
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestParam Long userId) {
        authService.logout(userId);
        return ResponseEntity.ok(ApiResponse.ok("Logged out successfully", null));
    }
}