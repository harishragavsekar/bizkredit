package com.bizkredit.controller;

import com.bizkredit.dto.*;
import com.bizkredit.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

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

    // Stateless logout — client discards token; server logs the action
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestParam Long userId) {
        authService.logout(userId);
        return ResponseEntity.ok(ApiResponse.ok("Logged out successfully", null));
    }

    // Step 1: request reset — returns 200 regardless of email existence.
    // In dev/test mode (bizkredit.security.expose-reset-token-in-response=true), the
    // response also carries the raw token so the reset flow can be exercised end-to-end
    // in Swagger without a configured email/SMS provider. Never enable that flag in a
    // real deployment — see application.properties for why.
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<ForgotPasswordResponse>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        var devToken = authService.forgotPassword(request.email());
        return ResponseEntity.ok(ApiResponse.ok(
                "If that email is registered, a reset token has been generated",
                new ForgotPasswordResponse(devToken.orElse(null))));
    }

    // Step 2: submit new password with token
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.ok(ApiResponse.ok("Password reset successfully", null));
    }
}
