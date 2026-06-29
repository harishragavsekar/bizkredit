package com.bizkredit.module1.service;

import com.bizkredit.common.config.JwtUtil;
import com.bizkredit.module1.dto.AuthResponse;
import com.bizkredit.module1.dto.LoginRequest;
import com.bizkredit.module1.dto.RegisterRequest;
import com.bizkredit.module1.entity.AuditLog;
import com.bizkredit.module1.entity.User;
import com.bizkredit.common.exception.BadRequestException;
import com.bizkredit.common.exception.ForbiddenException;
import com.bizkredit.module1.repository.AuditLogRepository;
import com.bizkredit.module1.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

// Handles authentication, registration, logout.
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService customUserDetailsService;

    private static final int MAX_FAILED_ATTEMPTS = 5;

    @Transactional
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Email already registered: " + request.email());
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .phone(request.phone())
                .role(request.role())
                .branchId(request.branchId())
                .status("Active")
                .failedLoginAttempts(0)
                .build();

        User saved = userRepository.save(user);

        saveAuditLog(saved.getUserId(), "REGISTER");
        log.info("User registered: {} [{}]", saved.getEmail(), saved.getRole());

        return buildAuthResponse(saved);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email(),
                            request.password()
                    )
            );
        } catch (BadCredentialsException e) {
            handleFailedLogin(request.email());
            throw e;
        }

        // Fetch user
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadRequestException("Invalid credentials"));

        if (!"Active".equals(user.getStatus())) {
            throw new ForbiddenException("Account is " + user.getStatus() + ". Contact admin.");
        }

        // Reset failed attempts
        user.setFailedLoginAttempts(0);
        userRepository.save(user);

        saveAuditLog(user.getUserId(), "LOGIN");
        log.info("User logged in: {}", user.getEmail());

        return buildAuthResponse(user);
    }

    @Transactional
    public void logout(Long userId) {
        saveAuditLog(userId, "LOGOUT");
        log.info("User logged out: {}", userId);
    }



    private void handleFailedLogin(String email) {

        userRepository.findByEmail(email).ifPresent(user -> {

            int attempts = getFailedAttempts(user) + 1;
            user.setFailedLoginAttempts(attempts);

            // Lock account after max attempts
            if (attempts >= MAX_FAILED_ATTEMPTS) {
                user.setStatus("Locked");
                log.warn("Account locked after {} attempts: {}", attempts, user.getEmail());
            }

            userRepository.save(user);
            saveAuditLog(user.getUserId(), "LOGIN_FAILED");
        });
    }

    private int getFailedAttempts(User user) {
        return user.getFailedLoginAttempts() == null ? 0 : user.getFailedLoginAttempts();
    }

    private AuthResponse buildAuthResponse(User user) {

        String token = buildToken(user);

        return new AuthResponse(
                token,
                "Bearer",
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }

    private String buildToken(User user) {

        var userDetails = customUserDetailsService.loadUserByUsername(user.getEmail());

        // JWT claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getUserId());
        claims.put("role", user.getRole().name());
        claims.put("branchId", user.getBranchId());

        return jwtUtil.generateToken(userDetails, claims);
    }

    private void saveAuditLog(Long userId, String action) {

        auditLogRepository.save(AuditLog.builder()
                .userId(userId)
                .action(action)
                .entityType("User")
                .recordId(String.valueOf(userId))
                .build());
    }
}