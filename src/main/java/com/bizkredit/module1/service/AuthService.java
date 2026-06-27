package com.bizkredit.module1.service;

import com.bizkredit.config.JwtUtil;
import com.bizkredit.module1.dto.AuthResponse;
import com.bizkredit.module1.dto.LoginRequest;
import com.bizkredit.module1.dto.RegisterRequest;
import com.bizkredit.module1.entity.AuditLog;
import com.bizkredit.module1.entity.User;
import com.bizkredit.exception.BadRequestException;
import com.bizkredit.exception.ForbiddenException;
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

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

// Handles authentication, registration, logout, and password reset operations
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

    // Allows reset token to be returned only in dev/test environment
    @Value("${bizkredit.security.expose-reset-token-in-response:false}")
    private boolean exposeResetTokenInResponse;

    private static final int MAX_FAILED_ATTEMPTS = 5;

    // Password rule: minimum 8 characters with uppercase, lowercase, digit, and special character
    private static final Pattern PASSWORD_POLICY = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,}$"
    );

    // Builds JWT token with user-specific claims
    private String buildToken(User user) {
        var userDetails = customUserDetailsService.loadUserByUsername(user.getEmail());

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getUserId());
        claims.put("role", user.getRole().name());
        claims.put("branchId", user.getBranchId());

        return jwtUtil.generateTokenWithClaims(userDetails, claims);
    }

    // Registers new user, stores encoded password, creates audit log, and returns JWT token
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
        log.info("User registered: {} [{}]", saved.getEmail(), saved.getRole());

        auditLogRepository.save(AuditLog.builder()
                .userId(saved.getUserId())
                .action("REGISTER")
                .entityType("User")
                .recordId(String.valueOf(saved.getUserId()))
                .build());

        String token = buildToken(saved);
        return new AuthResponse(token, "Bearer", saved.getUserId(),
                saved.getName(), saved.getEmail(), saved.getRole());
    }

    // Authenticates user using email/password and returns JWT token on success
    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            // Spring Security verifies email and password using AuthenticationManager
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        }
        catch (BadCredentialsException e) {
            // Increase failed login count and lock account after maximum failed attempts
            userRepository.findByEmail(request.email()).ifPresent(u -> {
                int attempts = (u.getFailedLoginAttempts() == null ? 0 : u.getFailedLoginAttempts()) + 1;
                u.setFailedLoginAttempts(attempts);
                if (attempts >= MAX_FAILED_ATTEMPTS) {
                    u.setStatus("Locked");
                    log.warn("Account locked after {} failed attempts: {}", attempts, u.getEmail());
                }
                userRepository.save(u);

                // Save failed login audit record
                auditLogRepository.save(AuditLog.builder()
                        .userId(u.getUserId())
                        .action("LOGIN_FAILED")
                        .entityType("User")
                        .recordId(String.valueOf(u.getUserId()))
                        .build());
            });
            throw e;
        }

        // Fetch authenticated user from database
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadRequestException("Invalid credentials"));

        // Prevent login for locked or inactive accounts
        if (!user.getStatus().equals("Active")) {
            throw new ForbiddenException("Account is " + user.getStatus() + ". Contact admin.");
        }

        // Reset failed attempts after successful login
        user.setFailedLoginAttempts(0);
        userRepository.save(user);

        // Generate JWT token after successful login
        String token = buildToken(user);
        log.info("User logged in: {}", user.getEmail());

        // Save successful login audit record
        auditLogRepository.save(AuditLog.builder()
                .userId(user.getUserId())
                .action("LOGIN")
                .entityType("User")
                .recordId(String.valueOf(user.getUserId()))
                .build());

        return new AuthResponse(token, "Bearer", user.getUserId(),
                user.getName(), user.getEmail(), user.getRole());
    }

    // Logs logout action; JWT invalidation is handled on client side in stateless authentication
    @Transactional
    public void logout(Long userId) {
        auditLogRepository.save(AuditLog.builder()
                .userId(userId)
                .action("LOGOUT")
                .entityType("User")
                .recordId(String.valueOf(userId))
                .build());
        log.info("User logged out: {}", userId);
    }

}